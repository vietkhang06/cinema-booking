package com.cinemabooking.backend.dto;

import com.google.cloud.firestore.annotation.Exclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {
    public static final String COLLECTION_NAME = "conversations";

    String convoId;
    List<String> participantIds;
    List<UserSnapShot> participants;

    Long lastMessageAt;
    ChatMessage lastMessage;

    Map<String, Integer> unreadCounts;
    Map<String, Long> lastSeenAt;

    String status; // "BOT_ONLY", "WAITING_STAFF", "ASSIGNED_TO_STAFF", "IN_PROGRESS", "RESOLVED", "CLOSED"
    String assignedStaffId;
    Boolean hadStaff;

    Long createdAt;
    Long updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSnapShot {
        String userId;
        String name;
        String email;
        String avatarUrl;

        public static UserSnapShot mapper (UserDTO userDTO) {
            return UserSnapShot.builder()
                    .userId(userDTO.getUid())
                    .name(userDTO.getName())
                    .email(userDTO.getEmail())
                    .avatarUrl(userDTO.getPhone())
                    .build();
        }
    }
}
