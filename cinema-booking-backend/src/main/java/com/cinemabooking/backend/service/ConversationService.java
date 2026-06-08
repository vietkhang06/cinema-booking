package com.cinemabooking.backend.service;

import com.cinemabooking.backend.dto.ChatMessage;
import com.cinemabooking.backend.dto.Conversation;
import com.cinemabooking.backend.dto.UserDTO;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteBatch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ConversationService {
    @Autowired
    private Firestore firestore;

    public List<Conversation> getConversationsWithUserDetail(String userId) throws ExecutionException, InterruptedException {
        List<Conversation> conversations = firestore.collection(Conversation.COLLECTION_NAME)
                .whereArrayContains("participantIds", userId)
                .get().get().toObjects(Conversation.class);

        conversations.sort((c1, c2) -> {
            Long t1 = c1.getLastMessageAt() != null ? c1.getLastMessageAt() : 0L;
            Long t2 = c2.getLastMessageAt() != null ? c2.getLastMessageAt() : 0L;
            return t2.compareTo(t1);
        });

        return conversations;
    }

    public Conversation createNewConversation(List<String> users, long now) throws ExecutionException, InterruptedException {
        DocumentReference documentReference = firestore.collection(Conversation.COLLECTION_NAME).document();

        Map<String, Integer> unreadCounts = new HashMap<>();
        for (String user : users) {
            unreadCounts.put(user, 0);
        }

        Map<String, Long> lastSeenAt = new HashMap<>();
        for (String user : users) {
            lastSeenAt.put(user, now);
        }

        List<Conversation.UserSnapShot> userSnapshots = firestore.collection(UserDTO.COLLECTION_NAME)
                .whereIn("uid", users)
                .get()
                .get().toObjects(UserDTO.class)
                .stream()
                .map(Conversation.UserSnapShot::mapper)
                .collect(Collectors.toList());

        Conversation conversation = Conversation.builder()
                .convoId(documentReference.getId())
                .participantIds(users)
                .unreadCounts(unreadCounts)
                .lastSeenAt(lastSeenAt)
                .participants(userSnapshots)
                .createdAt(now)
                .updatedAt(now)
                .build();

        documentReference.set(conversation);

        return conversation;
    }

    public void updateConversationAfterMessage(
            String convoId, ChatMessage message, String receiverId, long timestamp
    ) throws ExecutionException, InterruptedException {
        DocumentReference ref = firestore.collection(Conversation.COLLECTION_NAME).document(convoId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("lastMessage", message);
        updates.put("lastMessageAt", message.getSentAt());
        updates.put("updatedAt", timestamp);
        updates.put("unreadCounts." + receiverId, FieldValue.increment(1));

        ref.update(updates).get();
    }

    public void markAsRead(String convoId, String userId, long timestamp) throws ExecutionException, InterruptedException {
        DocumentReference ref = firestore.collection(Conversation.COLLECTION_NAME).document(convoId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("unreadCounts." + userId, 0);
        updates.put("lastSeenAt." + userId, timestamp);

        ref.update(updates).get();
    }

    public Conversation getConversationByUserIds(String user1, String user2) throws ExecutionException, InterruptedException {
        List<Conversation> senderConvo = firestore.collection(Conversation.COLLECTION_NAME)
                .whereArrayContains("participantIds", user1)
                .get().get().toObjects(Conversation.class);

        Conversation conversation = senderConvo.stream()
                .filter(c -> c.getParticipantIds().contains(user2))
                .findFirst()
                .orElse(null);

        return conversation;
    }

    public void deleteAllConversations() {
        WriteBatch batch = firestore.batch();
        firestore.collection(Conversation.COLLECTION_NAME).listDocuments()
                .forEach(doc -> batch.delete(doc));
        batch.commit();
    }

    public Conversation createSupportConversation(String customerId, long now) throws ExecutionException, InterruptedException {
        DocumentReference documentReference = firestore.collection(Conversation.COLLECTION_NAME).document();
        
        List<String> users = Arrays.asList(customerId, "SUPPORT_BOT");
        
        Map<String, Integer> unreadCounts = new HashMap<>();
        unreadCounts.put(customerId, 0);
        unreadCounts.put("SUPPORT_BOT", 0);

        Map<String, Long> lastSeenAt = new HashMap<>();
        lastSeenAt.put(customerId, now);
        lastSeenAt.put("SUPPORT_BOT", now);

        List<Conversation.UserSnapShot> userSnapshots = new ArrayList<>();
        DocumentSnapshot customerDoc = firestore.collection(UserDTO.COLLECTION_NAME).document(customerId).get().get();
        if (customerDoc.exists()) {
            UserDTO customerDTO = customerDoc.toObject(UserDTO.class);
            if (customerDTO != null) {
                userSnapshots.add(Conversation.UserSnapShot.mapper(customerDTO));
            }
        }
        
        userSnapshots.add(Conversation.UserSnapShot.builder()
                .userId("SUPPORT_BOT")
                .name("Trợ lý ảo (Bot)")
                .email("bot@cinemabooking.com")
                .avatarUrl("bot_avatar")
                .build());

        Conversation conversation = Conversation.builder()
                .convoId(documentReference.getId())
                .participantIds(users)
                .unreadCounts(unreadCounts)
                .lastSeenAt(lastSeenAt)
                .participants(userSnapshots)
                .status("BOT_ONLY")
                .createdAt(now)
                .updatedAt(now)
                .build();

        documentReference.set(conversation).get();

        ChatMessage welcomeMessage = ChatMessage.builder()
                .convoId(conversation.getConvoId())
                .senderId("SUPPORT_BOT")
                .receiverId(customerId)
                .content("Xin chào! Tôi là Trợ lý ảo của CinemaBookingApp. Tôi có thể giúp gì cho bạn hôm nay? Bạn có thể chọn các câu hỏi thường gặp bên dưới hoặc nhập câu hỏi trực tiếp.")
                .sentAt(now)
                .build();
        
        DocumentReference msgRef = firestore.collection(ChatMessage.COLLECTION_NAME).document();
        welcomeMessage.setMessageId(msgRef.getId());
        msgRef.set(welcomeMessage).get();
        
        conversation.setLastMessage(welcomeMessage);
        conversation.setLastMessageAt(now);
        documentReference.set(conversation).get();

        return conversation;
    }

    public Conversation getConversationById(String convoId) throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = firestore.collection(Conversation.COLLECTION_NAME).document(convoId).get().get();
        if (doc.exists()) {
            return doc.toObject(Conversation.class);
        }
        return null;
    }
}
