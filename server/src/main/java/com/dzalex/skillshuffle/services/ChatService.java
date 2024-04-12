package com.dzalex.skillshuffle.services;

import com.dzalex.skillshuffle.dtos.ChatDTO;
import com.dzalex.skillshuffle.dtos.ChatPreviewDTO;
import com.dzalex.skillshuffle.dtos.CommunityPreviewDTO;
import com.dzalex.skillshuffle.dtos.MessageDTO;
import com.dzalex.skillshuffle.entities.*;
import com.dzalex.skillshuffle.repositories.ChatMemberRepository;
import com.dzalex.skillshuffle.repositories.ChatRepository;
import com.dzalex.skillshuffle.repositories.CommunityChatRepository;
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
    private CommunityChatRepository communityChatRepository;

    @Autowired
    private CommunityService communityService;

    @Autowired
    private ChatMemberRepository chatMemberRepository;

    public List<ChatPreviewDTO> getChatList() {
        List<Chat> chats = chatRepository.findAll();
        List<ChatPreviewDTO> chatPreviewDTOs = new ArrayList<>();

        for (Chat chat : chats) {

            // Check if the current user is a member of the chat
            String authedUsername = userService.getCurrentUser().getUsername();
            if (!userService.getUsersInChat(chat.getId()).contains(authedUsername)) {
                continue;
            }

            // Get the last message of the chat
            MessageDTO lastMessage = messageService.findLastMessageByChatId(chat.getId());
            ChatPreviewDTO chatPreviewDTO = new ChatPreviewDTO();

            // Populate the DTO with chat information and last message
            chatPreviewDTO.setId(chat.getId());
            chatPreviewDTO.setName(chat.getName());
            chatPreviewDTO.setType(chat.getType());
            chatPreviewDTO.setAvatarUrl(chat.getAvatarUrl());
            chatPreviewDTO.setLastMessage(lastMessage);

            chatPreviewDTOs.add(chatPreviewDTO);
        }

        return chatPreviewDTOs;
    }

    public ChatDTO getChatWithMessages(Chat chat) {
        // Get chat info depending on the chat type
        ChatDTO chatDTO = getChatInfo(chat);

        // Load only 30 last messages
        chatDTO.setMessages(getChatMessages(chat.getId(), 30, 0));

        return chatDTO;
    }

    // Get chat messages with limit and offset parameters
    public List<MessageDTO> getChatMessages(Integer chatId, int limit, int offset) {
        List<ChatMessage> messages = messageRepository.findMessagesByChatId(chatId);
        List<MessageDTO> messageDTOs = new ArrayList<>();

        // Sort messages by latest timestamp
        messages.sort(Comparator.comparing(ChatMessage::getTimestamp).reversed());

        // Add messages to the list with limit and offset
        for (int i = offset; i < messages.size() && i < offset + limit; i++) {
            messageDTOs.add(messageService.convertToDTO(messages.get(i)));
        }

        // Sort messages by timestamp in ascending order
        messageDTOs.sort(Comparator.comparing(MessageDTO::getTimestamp));

        return messageDTOs;
    }

    // Get chat info
    public ChatDTO getChatInfo(Chat chat) {
        User authedUser = userService.getCurrentUser();
        ChatDTO chatDTO = new ChatDTO();
        chatDTO.setId(chat.getId());
        chatDTO.setType(chat.getType());

        switch (chat.getType()) {
            case COMMUNITY:
                CommunityChat communityChat = communityChatRepository.findCommunityChatByChatId(chat.getId());
                CommunityPreviewDTO community = communityService.convertToPreviewDTO(communityChat.getCommunity());
                chatDTO.setCommunity(community);
                chatDTO.setAvatarUrl(community.getAvatarUrl());
                chatDTO.setName(community.getName());
                break;
            case PRIVATE:
                chatMemberRepository.findAllByChatId(chat.getId()).forEach(chatMember -> {
                    if (!chatMember.getMember().getUsername().equals(authedUser.getUsername())) {
                        User chatPartner = chatMember.getMember();
                        chatDTO.setUser(chatPartner);
                        chatDTO.setAvatarUrl(chatPartner.getAvatarUrl());
                        chatDTO.setName(chatPartner.getFirstName() + " " + chatPartner.getLastName());
                    }
                });
                break;
            case GROUP:
                chatDTO.setMemberCount(userService.getUsersInChat(chat.getId()).size());
                chatDTO.setAvatarUrl(chat.getAvatarUrl());
                chatDTO.setName(chat.getName());
                break;
        }

        return chatDTO;
    }
}
