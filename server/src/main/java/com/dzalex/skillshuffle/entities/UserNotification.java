package com.dzalex.skillshuffle.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "user_notifications")
public class UserNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ColumnDefault("1")
    @Column(name = "likes_notification", nullable = false)
    private Boolean likesNotification;

    @ColumnDefault("1")
    @Column(name = "comments_notification", nullable = false)
    private Boolean commentsNotification;

    @ColumnDefault("1")
    @Column(name = "reposted_notification", nullable = false)
    private Boolean repostedNotification;

    @ColumnDefault("1")
    @Column(name = "private_messages_notification", nullable = false)
    private Boolean privateMessagesNotification;

    @ColumnDefault("1")
    @Column(name = "friend_requests_notification", nullable = false)
    private Boolean friendRequestsNotification;

    @ColumnDefault("1")
    @Column(name = "mentions_notification", nullable = false)
    private Boolean mentionsNotification;

    @ColumnDefault("1")
    @Column(name = "followers_notification", nullable = false)
    private Boolean followersNotification;

}