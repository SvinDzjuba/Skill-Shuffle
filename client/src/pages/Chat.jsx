import React, { useEffect, useState, useLayoutEffect, useRef, useCallback } from 'react';
import { Row, Stack, Button, Container } from 'react-bootstrap';
import { useNavigate, useLocation } from 'react-router-dom';
import { Client } from '@stomp/stompjs';
import axios from 'axios';

import { AESEncrypt, AESDecrypt } from '../crypto';
import { API_SERVER } from '../config';
import { useAuth } from '../components/AuthContext';
import MessageRenderer from '../components/MessageRenderer';
import ChatPreview from '../components/ChatPreview';
import ChatMenu from '../components/ChatMenu';
import ChatTypeFilter from '../components/ChatTypeFilter';
import ChatHeader from '../components/ChatHeader';

import EmojiGifPicker from '../components/EmojiGifPicker';
import ChatBackground from '../assets/images/chat-background.jpg'

function Chat() {
  const { authUser, stompClient } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [scrollPosition, setScrollPosition] = useState(0);
  const [loading, setLoading] = useState(false);
  const [chats, setChats] = useState([]);
  const [filteredChats, setFilteredChats] = useState([]);
  const [choosenChat, setChoosenChat] = useState({});
  // const [stompClient, setStompClient] = useState(null);
  const [messageContent, setMessageContent] = useState('');
  // const [messageNotification, setMessageNotification] = useState({ visible: false, heading: '', message: {}, to: '' });
  const messagesListRef = useRef(null);
  const subscriptionRef = useRef(null);
  // const timeoutRef = useRef(null);
  // const debounceTimeout = useRef(null);
  const firstMessageRef = useRef(null);
  const offsetRef = useRef(0);
  const limit = 30;


  const updateChatLastMessage = useCallback((message) => {
    message = message.content === undefined ? message[message.length - 1] : message;
    setChats(prevChats => {
      return prevChats.map(chat => {
        if (AESDecrypt(chat.id) === message.chat.id.toString()) {
          return {
            ...chat,
            last_message: { ...chat.last_message, content: message.content }
          };
        }
        return chat;
      });
    });
  }, []);

  const subscribeToChatMessages = useCallback(() => {
    if (stompClient && choosenChat.id) {
      const destination = `/user/chat/${choosenChat.id}`;
      if (subscriptionRef.current) {
        subscriptionRef.current.unsubscribe(); // Unsubscribe from previous chat
      }
      const newSubscription = stompClient.subscribe(destination, receivedMessage => {
        const message = JSON.parse(receivedMessage.body);
        updateChatLastMessage(message);
        setChoosenChat(prevChat => ({
          ...prevChat,
          messages: [...prevChat.messages, message]
        }));
      });
      subscriptionRef.current = newSubscription; // Store the new subscription
    }
  }, [stompClient, choosenChat.id, updateChatLastMessage]);

  // const showNotification = useCallback((notification) => {
  //   setMessageNotification({ visible: true, notification: notification });
  //   clearTimeout(timeoutRef.current);
  //   timeoutRef.current = setTimeout(() => {
  //     setMessageNotification(prevState => ({
  //       ...prevState,
  //       visible: false,
  //     }));
  //   }, 5000);
  // }, []);

  // const subscribeToNotifications = useCallback(() => {
  //   return stompClient.subscribe(`/user/notification`, receivedNotification => {
  //     const notification = JSON.parse(receivedNotification.body);
  //     if (notification.type === 'CHAT_MESSAGE' && notification.chat.id !== choosenChat.id) {
  //       updateChatLastMessage(notification);
  //       clearTimeout(debounceTimeout.current);
  //       debounceTimeout.current = setTimeout(() => {
  //         showNotification(notification);
  //         debounceTimeout.current = null;
  //       }, 1000);
  //     }
  //   });
  // }, [stompClient, choosenChat.id, updateChatLastMessage, showNotification]);

  const getChatMessages = useCallback((chatId) => {
    axios.get(`${API_SERVER}/chats/${chatId}`, { withCredentials: true })
      .then(response => {
        setChoosenChat(response.data);
        offsetRef.current = 0;
      })
      .catch(error => {
        console.error(error.response?.data.message || error.message);
      });
  }, []);

  const sendMessage = (gif) => {
    if (messageContent === '' && !gif) return;
    setMessageContent('');

    const destination = `/app/chat/${choosenChat.id}`;
    const message = {
      sender: authUser,
      chat: { id: choosenChat.id },
      content: gif ? gif.url : messageContent,
      timestamp: new Date().toISOString(),
    };
    if (stompClient != null) {
      // Send the message to the server to handle
      stompClient.publish({
        destination,
        body: JSON.stringify(message),
      });
    }
  };

  const loadMoreMessages = async () => {
    setLoading(true);
    const prevScrollHeight = messagesListRef.current.scrollHeight;
    const prevScrollTop = messagesListRef.current.scrollTop;
    offsetRef.current += limit;
    try {
      const response = await axios.get(`${API_SERVER}/chats/${choosenChat.id}/messages?offset=${offsetRef.current}&limit=${limit}`, { withCredentials: true });
      const newMessages = response.data;
      setChoosenChat(prevChat => ({
        ...prevChat,
        messages: [...newMessages, ...prevChat.messages],
      }));

      // Calculate the difference in scroll height
      const newScrollHeight = messagesListRef.current.scrollHeight;
      const scrollHeightDifference = newScrollHeight - prevScrollHeight;

      // Adjust scroll position to maintain the relative position of the first visible message
      messagesListRef.current.scrollTop = prevScrollTop + scrollHeightDifference;
      setLoading(false);
    } catch (error) {
      console.error('Error loading more messages:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleScroll = () => {
    const { scrollTop } = messagesListRef.current;
    if (scrollTop === 0 && !loading) {
      loadMoreMessages();
    }
    setScrollPosition(scrollTop);
  };

  const scrollToPosition = (position) => {
    if (messagesListRef.current) {
      messagesListRef.current.scrollTop = position;
    }
  };

  const openChatMenu = () => {
    // 
  };

  const formatTimestampForMessage = (timestamp) => {
    const date = new Date(timestamp);
    // hh:mm for the time of the message
    return date.toLocaleTimeString('en-GB', { hour: 'numeric', minute: 'numeric' });
  }

  // ---------------------------- //

  useEffect(() => {
    if (messagesListRef.current) {
      setScrollPosition(messagesListRef.current.scrollHeight);
    }
  }, [choosenChat]);

  useLayoutEffect(() => {
    scrollToPosition(scrollPosition);
  }, [choosenChat, scrollPosition]);

  // useEffect(() => {
  //   axios.get(`${API_SERVER}/auth/confirm`, { withCredentials: true })
  //     .then(response => {
  //       const newClient = new Client();
  //       newClient.configure({
  //         brokerURL: `ws://${WEBSOCKET_URL}`,
  //         connectHeaders: {
  //           Authorization: `Bearer ${response.data.access_token}`
  //         },
  //       });
  //       newClient.activate();
  //       setStompClient(newClient);
  //     })
  //     .catch(() => {
  //       setAuthUser(null);
  //       navigate('/sign-in');
  //     });
  // }, [setStompClient, setAuthUser, navigate]);

  // Handle the WebSocket connection
  useEffect(() => {
    if (stompClient != null && choosenChat.id) {
      const onConnectCallback = () => {
        // Subscribe to the current chat messages
        subscribeToChatMessages();
      };
      stompClient.onConnect = onConnectCallback;
      if (stompClient.connected) {
        onConnectCallback();
      }
    }
  }, [stompClient, choosenChat, subscribeToChatMessages]);

  // Get the current chat via the encrypted chat ID in the URL
  useEffect(() => {
    const encryptedChatId = new URLSearchParams(location.search).get('c');
    if (encryptedChatId) {
      try {
        const decryptedChatId = AESDecrypt(encryptedChatId);
        if (decryptedChatId !== '' && !isNaN(decryptedChatId)) {
          getChatMessages(decryptedChatId);
        }
      } catch (error) {
        console.error("Error decrypting chat: ", error);
      }
    }
  }, [location.search, getChatMessages]);

  useEffect(() => {
    axios.get(`${API_SERVER}/chats`, { withCredentials: true })
      .then(response => {
        response.data.forEach(chat => {
          chat.id = AESEncrypt(chat.id.toString());
        });
        setChats(response.data);
        setFilteredChats(response.data);
      })
      .catch(error => {
        console.error(error.response?.data.message || error.message);
      });
  }, []);

  return (
    <div className="chat-page w-100 d-flex">
      <Container className="chat-menu">

        <ChatTypeFilter setChats={setFilteredChats} chats={chats} />

        <Stack direction='vertical'>
          {filteredChats.map(chat => (
            <ChatPreview
              chat={chat}
              navigate={navigate}
              key={chat.id}
            />
          ))}
        </Stack>

      </Container>

      {choosenChat.id === undefined ? (
        // If no chat is selected, display:
        <div className='chat-box non-selected' key={choosenChat.id} style={{ backgroundImage: `url(${ChatBackground})` }}>
          <p className='no-chat-selected'>Select a chat to start messaging</p>
        </div>
      ) :
        // If a chat is selected, display:
        <div className='chat-box d-flex flex-nowrap flex-column' key={choosenChat.id}>

          <ChatHeader chat={choosenChat} openChatMenu={openChatMenu} />

          <Row className='messages-list p-0 py-3' ref={messagesListRef} onScroll={handleScroll}>
            <Stack direction='vertical' gap={2}>
              {choosenChat.messages && choosenChat.messages.map((message, index) => (
                <div
                  className={`message d-flex flex-wrap ${message.sender.nickname === authUser.nickname ? 'own-message' : 'other-message'} ${index > 0 && message.sender.nickname === choosenChat.messages[index - 1].sender.nickname ? '' : 'mt-2'}`}
                  key={index}
                  ref={index === 0 ? firstMessageRef : null}
                >
                  <MessageRenderer
                    message={message}
                    index={index}
                    authUser={authUser}
                    chat={choosenChat}
                    formatTimestamp={formatTimestampForMessage}
                  />
                </div>
              )
              )}
            </Stack>
          </Row>

          <Row className="message-input p-0 d-flex">
            <div className="input-container p-0">
              <input
                type='text'
                name='message'
                className='w-100 h-100'
                placeholder='Type a message...'
                value={messageContent}
                onChange={(event) => setMessageContent(event.target.value)}
                onKeyDown={(event) => {
                  if (event.key === 'Enter') {
                    sendMessage(undefined);
                  }
                }}
                maxLength={1024}
              />
              <label
                htmlFor="message"
                className={`length-counter d-flex align-items-center ${messageContent.length === 1024 ? 'over-limit' : ''}`}
              >
                {messageContent.length}/1024
              </label>
            </div>

            <EmojiGifPicker setMessageContent={setMessageContent} sendMessage={sendMessage} />

            <Button variant='outline-primary' className='btn-send' onClick={() => sendMessage(undefined)}>
              Send
            </Button>
          </Row>
        </div>
      }
    </div>
  );
}

export default Chat;