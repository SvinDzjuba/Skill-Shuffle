package com.dzalex.skillshuffle.dtos;

import com.dzalex.skillshuffle.enums.MemberRole;
import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMemberDTO {
    private String firstName;
    private String lastName;
    private String nickname;
    private String avatarUrl;
    private Timestamp lastSeen;
    private MemberRole role;
    private Boolean isLeft;
}
