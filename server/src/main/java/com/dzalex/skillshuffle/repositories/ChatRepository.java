package com.dzalex.skillshuffle.repositories;

import com.dzalex.skillshuffle.entities.Chat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRepository extends JpaRepository<Chat, Integer> {
    Chat findChatById(Integer id);
}
