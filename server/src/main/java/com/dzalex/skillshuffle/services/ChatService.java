package com.dzalex.skillshuffle.services;

import com.dzalex.skillshuffle.dtos.ChatDTO;
import com.dzalex.skillshuffle.dtos.ChatPreviewDTO;
import com.dzalex.skillshuffle.dtos.MessageDTO;
import com.dzalex.skillshuffle.models.Chat;
import com.dzalex.skillshuffle.models.Message;
import com.dzalex.skillshuffle.repositories.ChatMemberRepository;
import com.dzalex.skillshuffle.repositories.ChatRepository;
import com.dzalex.skillshuffle.repositories.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ChatService {

    @Autowired
    private MessageService messageService;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ChatMemberRepository chatMemberRepository;

    public List<ChatPreviewDTO> getChatListWithLastMessage() {
        List<Chat> chats = chatRepository.findAll();

        // Get chat members and remove chat_member if user is not in the chat, then get chats again with updated list
        chatMemberRepository.findAll().forEach(chatMember -> {
            if (userService.getCurrentUser() == null || !chatMember.getMember().getUsername().equals(userService.getCurrentUser().getUsername())) {
                chats.remove(chatMember.getChat());
            }
        });

        List<ChatPreviewDTO> chatPreviewDTOs = new ArrayList<>();
        for (Chat chat : chats) {
            MessageDTO lastMessage = messageService.findLastMessageByChatId(chat.getId());
            ChatPreviewDTO chatPreviewDTO = new ChatPreviewDTO();

            // Populate the DTO with chat information and last message
            chatPreviewDTO.setId(chat.getId());
            chatPreviewDTO.setName(chat.getName());
            chatPreviewDTO.setType(chat.getType());
            chatPreviewDTO.setAvatar_url(chat.getAvatar_url());
            chatPreviewDTO.setLast_message(lastMessage);

            chatPreviewDTOs.add(chatPreviewDTO);
        }

        return chatPreviewDTOs;
    }

    public ChatDTO getChatWithMessages(Chat chat) {
        ChatDTO chatDTO = new ChatDTO();
        chatDTO.setId(chat.getId());
        chatDTO.setName(chat.getName());
        chatDTO.setType(chat.getType());
        chatDTO.setAvatar_url(chat.getAvatar_url());

        // Load only 30 last messages
        chatDTO.setMessages(getChatMessages(chat.getId(), 30, 0));

        return chatDTO;
    }

    // Get chat messages with limit and offset parameters
    public List<MessageDTO> getChatMessages(Long chatId, int limit, int offset) {
        List<Message> messages = messageRepository.findMessagesByChatId(chatId);
        List<MessageDTO> messageDTOs = new ArrayList<>();

        // Sort messages by latest timestamp
        messages.sort(Comparator.comparing(Message::getTimestamp).reversed());

        // Add messages to the list with limit and offset
        for (int i = offset; i < messages.size() && i < offset + limit; i++) {
            messageDTOs.add(messageService.convertToDTO(messages.get(i)));
        }

        // Sort messages by timestamp in ascending order
        messageDTOs.sort(Comparator.comparing(MessageDTO::getTimestamp));

        return messageDTOs;
    }
}
