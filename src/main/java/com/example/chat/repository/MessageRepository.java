package com.example.chat.repository;


import com.example.chat.model.Message;
import com.example.chat.model.NewMessage;
import jakarta.annotation.Nullable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository {
    List<Message> findLatest(String channelId, int fetchMax, @Nullable String lastSeenMessageId);

    default List<Message> findLatest(String channelId, int fetchMax) {
        return findLatest(channelId, fetchMax, null);
    }

    Message save(NewMessage newMessage);
}