package com.dzalex.skillshuffle.services;

import com.dzalex.skillshuffle.dtos.*;
import com.dzalex.skillshuffle.entities.*;
import com.dzalex.skillshuffle.enums.ChatAnnouncementType;
import com.dzalex.skillshuffle.enums.ChatType;
import com.dzalex.skillshuffle.enums.MemberRole;
import com.dzalex.skillshuffle.enums.MessageType;
import com.dzalex.skillshuffle.repositories.ChatMemberRepository;
import com.dzalex.skillshuffle.repositories.ChatRepository;
import com.dzalex.skillshuffle.repositories.CommunityChatRepository;
import com.dzalex.skillshuffle.repositories.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

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

    @Autowired
    private FileService fileService;

    public List<ChatPreviewDTO> getChatList() {
        User authedUser = userService.getCurrentUser();
        List<Chat> chats = chatRepository.findAll();
        List<ChatPreviewDTO> chatPreviewDTOs = new ArrayList<>();

        for (Chat chat : chats) {
            if (isUserMemberOfChat(chat, authedUser)) {
                // Get chat info based on the chat type
                ChatDTO chatInfo = getChatInfo(chat);

                ChatPreviewDTO chatPreviewDTO = new ChatPreviewDTO();
                chatPreviewDTO.setId(chat.getId());
                chatPreviewDTO.setType(chat.getType());
                chatPreviewDTO.setName(chatInfo.getName());
                chatPreviewDTO.setAvatarUrl(chatInfo.getAvatarUrl());
                chatPreviewDTO.setLastMessage(messageService.findLastMessageByChatId(chat.getId()));

                chatPreviewDTOs.add(chatPreviewDTO);
            }
        }

        return chatPreviewDTOs;
    }

    public ChatDTO getChatWithMessages(Chat chat) {
        if (!isUserMemberOfChat(chat, userService.getCurrentUser())) {
            throw new IllegalArgumentException("User is not a member of this chat");
        }

        // Get chat info depending on the chat type
        ChatDTO chatDTO = getChatInfo(chat);

        // Load only 30 last messages
        chatDTO.setMessages(getChatMessages(chat.getId(), 30, 0));

        return chatDTO;
    }

    // Get chat messages with limit and offset parameters
    public List<MessageDTO> getChatMessages(Integer chatId, int limit, int offset) {
        if (limit < 0 || offset < 0) {
            throw new IllegalArgumentException("Limit and offset must be non-negative");
        }

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

    public ChatDTO getChatInfo(Chat chat) {
        ChatDTO chatDTO = new ChatDTO();
        chatDTO.setId(chat.getId());
        chatDTO.setType(chat.getType());
        chatDTO.setMessages(getChatMessages(chat.getId(), 30, 0));

        switch (chat.getType()) {
            case COMMUNITY -> setCommunityChatInfo(chat, chatDTO);
            case PRIVATE -> setPrivateChatInfo(chat, chatDTO);
            case GROUP -> setGroupChatInfo(chat, chatDTO);
        }

        return chatDTO;
    }

    private void setCommunityChatInfo(Chat chat, ChatDTO chatDTO) {
        CommunityChat communityChat = communityChatRepository.findCommunityChatByChatId(chat.getId());
        CommunityPreviewDTO community = communityService.convertToPreviewDTO(communityChat.getCommunity());
        chatDTO.setAvatarUrl(community.getAvatarUrl());
        chatDTO.setName(community.getName());
        chatDTO.setCommunity(community);
    }

    private void setPrivateChatInfo(Chat chat, ChatDTO chatDTO) {
        chatMemberRepository.findAllByChatId(chat.getId()).stream()
            .filter(chatMember -> !chatMember.getMember().getUsername().equals(userService.getCurrentUser().getUsername()))
            .findFirst()
            .ifPresent(chatMember -> {
                User chatPartner = chatMember.getMember();
                chatDTO.setAvatarUrl(chatPartner.getAvatarUrl());
                chatDTO.setName(chatPartner.getFirstName() + " " + chatPartner.getLastName());
                List<ChatMemberDTO> members = new ArrayList<>();
                members.add(userService.getChatMemberDTO(chatPartner, chatMember.getRole()));
                chatDTO.setMembers(members);
            });
    }

    private void setGroupChatInfo(Chat chat, ChatDTO chatDTO) {
        chatDTO.setMembers(userService.getUsersInChat(chat.getId()));
        chatDTO.setAvatarUrl(chat.getAvatarUrl());
        chatDTO.setName(chat.getName());
    }

    // Create a new chat
    public Chat createChat(NewChatDTO chat, MultipartFile avatarBlob) {
        if (chat.getName().isEmpty() && chat.getType() == ChatType.GROUP) {
            List<String> firstNames = userService.getUsersFirstNameInChat(chat.getMembers());
            if (firstNames.size() > 3) {
                int size = firstNames.size();
                firstNames = firstNames.subList(0, 3);
                firstNames.add("and " + (size - 3) + " more");
            }
            chat.setName(String.join(", ", firstNames));
        }

        Chat newChat = Chat.builder()
                .name(chat.getName())
                .type(chat.getType())
                .avatarUrl(null)
                .build();
        chatRepository.save(newChat);

        // Save the avatar image if provided
        if (avatarBlob != null) {
            String avatarFilePath = "chats/chat-" + newChat.getId() + "/avatar/";
            // Upload the image to the S3 bucket
            String avatarUrl = fileService.uploadFile(avatarBlob, avatarFilePath);
            if (avatarUrl != null) {
                newChat.setAvatarUrl(avatarUrl);
            }
            chatRepository.save(newChat);
        }

        User authedUser = userService.getCurrentUser();

        // Add the current user to the chat
        addMemberToChat(newChat, authedUser, getRoleByChatType(chat.getType()));

        // Add members to the chat
        Arrays.stream(chat.getMembers())
              .map(userService::getUserByNickname)
              .forEach(user -> addMemberToChat(newChat, user, MemberRole.MEMBER));

        // Send ENTRY message to the chat
        if (chat.getType() == ChatType.GROUP) {
            messageService.createAnnouncementMessage(authedUser, newChat, ChatAnnouncementType.CREATED, null);
        } else {
            messageService.createEntryMessage(authedUser, newChat);
        }

        return newChat;
    }

    private void addMemberToChat(Chat chat, User user, MemberRole role) {
        ChatMember chatMember = ChatMember.builder()
                .chat(chat)
                .member(user)
                .role(role)
                .notifications(true)
                .build();
        chatMemberRepository.save(chatMember);
    }

    private MemberRole getRoleByChatType(ChatType chatType) {
        return chatType == ChatType.GROUP ? MemberRole.CREATOR : MemberRole.MEMBER;
    }

    private boolean isUserMemberOfChat(Chat chat, User user) {
        if (chat.getType() == ChatType.COMMUNITY) {
            CommunityChat communityChat = communityChatRepository.findCommunityChatByChatId(chat.getId());
            return communityChat != null && Objects.equals(communityChat.getUser().getId(), user.getId());
        } else {
            ChatMember chatMember = chatMemberRepository.findChatMemberByChatIdAndMemberId(chat.getId(), user.getId());
            return chatMember != null;
        }
    }

    @Transactional
    public void deleteEmptyChatsByAuthedUser(User authedUser) {
        // Delete chats with only one ENTRY type message
        chatRepository.findAll().forEach(chat -> {
            List<ChatMessage> messages = messageRepository.findMessagesByChatId(chat.getId());
            if (messages.size() == 1 && messages.get(0).getType() == MessageType.ENTRY
                    && Objects.equals(messages.get(0).getSender().getId(), authedUser.getId()))
            {
                if (chat.getType() == ChatType.COMMUNITY) {
                    communityChatRepository.deleteAllByChatId(chat.getId());
                } else {
                    chatMemberRepository.deleteAllByChatId(chat.getId());
                }
                messageRepository.delete(messages.get(0));
                chatRepository.delete(chat);
            }
        });
    }

    // Delete chat by id
    public void deleteChatById(Integer id) {
        Chat chat = chatRepository.findChatById(id);
        if (chat != null) {
            chatMemberRepository.deleteAllByChatId(id);
            messageRepository.deleteAllByChatId(id);
            chatRepository.delete(chat);
        }
    }

    // Leave chat
    public void leaveChat(Chat chat) {
        User authedUser = userService.getCurrentUser();
        ChatMember chatMember = chatMemberRepository.findChatMemberByChatIdAndMemberId(chat.getId(), authedUser.getId());
        if (chatMember != null) {
            chatMemberRepository.delete(chatMember);
            messageService.createAnnouncementMessage(authedUser, chat, ChatAnnouncementType.LEFT, null);

            // If the leaving user is the creator, transfer ownership to another member
            if (chatMember.getRole() == MemberRole.CREATOR) {

                // If there is any admins, transfer ownership to the first admin
                ChatMember firstAdmin = chatMemberRepository.findFirstByChatIdAndRole(chat.getId(), MemberRole.ADMIN);
                if (firstAdmin != null) {
                    firstAdmin.setRole(MemberRole.CREATOR);
                    chatMemberRepository.save(firstAdmin);
                    return;
                }

                // If there is any members, transfer ownership to the first member
                ChatMember newOwner = chatMemberRepository.findFirstByChatIdAndRole(chat.getId(), MemberRole.MEMBER);
                if (newOwner != null) {
                    newOwner.setRole(MemberRole.CREATOR);
                    chatMemberRepository.save(newOwner);
                }
            }
        }
    }

    // Remove member from chat
    public void removeMemberFromChat(Chat chat, User user) {
        ChatMember chatMember = chatMemberRepository.findChatMemberByChatIdAndMemberId(chat.getId(), user.getId());
        if (chatMember != null) {
            chatMemberRepository.delete(chatMember);
            messageService.createAnnouncementMessage(userService.getCurrentUser(), chat, ChatAnnouncementType.REMOVED, user);
        }
    }

    // Patch method to update chat avatar
    public Chat updateChatAvatar(Chat chat, MultipartFile avatarBlob) {
        String avatarFilePath = "chats/chat-" + chat.getId() + "/avatar/";
        String avatarUrl;
        if (chat.getAvatarUrl() == null) {
            avatarUrl = fileService.uploadFile(avatarBlob, avatarFilePath);
        } else {
            avatarUrl = fileService.changeFile(avatarBlob, avatarFilePath, chat.getAvatarUrl());
        }
        if (avatarUrl != null) {
            chat.setAvatarUrl(avatarUrl);
            chatRepository.save(chat);
            return chat;
        }
        return null;
    }

    public Chat inviteMembersToChat(Chat chat, List<String> users) {
        if (chat.getType() == ChatType.GROUP) {
            users.stream()
                 .map(userService::getUserByNickname)
                 .forEach(user -> {
                     addMemberToChat(chat, user, MemberRole.MEMBER);
                     messageService.createAnnouncementMessage(userService.getCurrentUser(), chat, ChatAnnouncementType.ADDED, user);
                 });
            return chat;
        }
        return null;
    }
}
