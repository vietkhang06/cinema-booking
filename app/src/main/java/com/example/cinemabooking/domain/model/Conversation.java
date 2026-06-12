package com.example.cinemabooking.domain.model;

import java.util.List;
import java.util.Map;

public class Conversation {
    public String convoId;
    public List<String> participantIds;
    public ChatMessage lastMessage;
    public Long lastMessageAt;
    public Map<String, Integer> unreadCounts;
    public Map<String, Long> lastSeenAt;

    public List<UserSnapShot> participants;

    public String status;
    public String assignedStaffId;
    public Boolean hadStaff;

    public Long createdAt;
    public Long updatedAt;

    public Conversation(){ }

    public static class UserSnapShot {
        public String userId;
        public String name;
        public String email;
        public String avatarUrl;

        public UserSnapShot(){ }
        public UserSnapShot(User user)  {
            this.userId = user.uid;
            this.name = user.name;
            this.email = user.email;
            this.avatarUrl = user.avatarUrl;
        }
    }
}
