package com.dzalex.skillshuffle.dtos;

import com.dzalex.skillshuffle.enums.ChatType;
import com.dzalex.skillshuffle.models.Message;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatDTO {
    private Long id;
    private String name;
    private ChatType chatType;
    private String avatar_url;
    private List<MessageDTO> messages;
}