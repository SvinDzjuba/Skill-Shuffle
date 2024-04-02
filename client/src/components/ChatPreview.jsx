import React from 'react';
import { Row, Col } from 'react-bootstrap';

import CreateImage from './CreateImage';

function ChatPreview({ chat, navigate }) {

    const formatTimestampForChatContainer = (timestamp) => {
        const date = new Date(timestamp);
        const currentDate = new Date();
        const difference = currentDate - date;

        if (difference > 31536000000) {
            // dd/mmm/yyyy if the message was sent more than a year ago
            return date.toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' });
        } else if (difference > 86400000) {
            // dd/mmm if the message was sent less than a year ago but more than a day ago
            return date.toLocaleDateString('en-GB', { day: 'numeric', month: 'short' });
        } else {
            // hh:mm if the message was sent less than a day ago
            return date.toLocaleTimeString('en-GB', { hour: 'numeric', minute: 'numeric' });
        }
    }

    return (
        <Row className='chat-preview d-flex align-items-center flex-nowrap'
            key={chat.id}
            role='button'
            onClick={() => { navigate(`/messenger?c=${chat.id}`); }}
        >
            <Col className='chat-avatar d-flex justify-content-center'>
                <CreateImage
                    url={chat.avatar_url}
                    alt={chat.name}
                    width={55}
                    height={55}
                    rounded={true}
                />
            </Col>
            <Col className='chat-info w-75 ps-3'>
                <p className='chat-name d-flex justify-content-between'>
                    {chat.name}
                    <span>{formatTimestampForChatContainer(chat.last_message.timestamp)}</span>
                </p>
                <p className='last-message text-truncate'>{chat.last_message.content}</p>
            </Col>
        </Row>
    )
}

export default ChatPreview