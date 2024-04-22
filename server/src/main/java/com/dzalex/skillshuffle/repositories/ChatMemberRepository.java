package com.dzalex.skillshuffle.repositories;

import com.dzalex.skillshuffle.entities.ChatMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMemberRepository extends JpaRepository<ChatMember, Integer> {
    List<ChatMember> findAllByChatId(Integer chatId);
    void deleteAllByChatId(Integer id);
    ChatMember findChatMemberByChatIdAndMemberId(Integer id, Integer id1);
}
