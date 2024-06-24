package com.example.chat.repository.impl;

import com.example.chat.model.Message;
import com.example.chat.model.NewMessage;
import com.example.chat.repository.MessageRepository;
import jakarta.annotation.Nullable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
public class InMemoryMessageRepository implements MessageRepository {

    private ConcurrentMap<String, MessageArchive> storage = new ConcurrentHashMap<>();

    @Override
    public List<Message> findLatest(String channelId, int fetchMax, @Nullable String lastSeenMessageId) {
        if (fetchMax < 1) {
            throw new IllegalArgumentException("Can't fetch less than 1 message");
        }

        return Optional.ofNullable(storage.get(channelId))
                .map(archive -> archive.findLatest(fetchMax, lastSeenMessageId))
                .orElse(Collections.emptyList());
    }

    @Override
    public Message save(NewMessage newMessage) {
        return storage.computeIfAbsent(newMessage.getChannelId(), MessageArchive::new)
                .save(newMessage);
    }

    private static class MessageArchive {
        private AtomicLong sequentialNumber = new AtomicLong(1);
        private String channelId;
        private List<Message> messages = new ArrayList<>();
        private ReadWriteLock lock = new ReentrantReadWriteLock();

        private MessageArchive(String channelId) {
            this.channelId = channelId;
        }

        public List<Message> findLatest(int fetchMax, @Nullable String lastSeenMessageId) {
            lock.readLock().lock();

            try {
                int indexOfLastSeen = Objects.isNull(lastSeenMessageId) ? -1 : indexOfMessage(lastSeenMessageId);

                if (messages.size() - fetchMax > indexOfLastSeen) {
                    return List.copyOf(messages.subList(messages.size() - fetchMax, messages.size()));
                } else {
                    return List.copyOf(messages.subList(indexOfLastSeen + 1, messages.size()));
                }
            } finally {
                lock.readLock().unlock();
            }
        }

        private int indexOfMessage(String messageId) {
            for (int i = messages.size() - 1; i >= 0; i--) {
                if (messages.get(i).getMessageId().equals(messageId)) {
                    return i;
                }
            }

            return -1;
        }

        private Message save(NewMessage message) {
            lock.writeLock().lock();

            try {
                var saved = new Message();
                saved.setMessageId(UUID.randomUUID().toString());
                saved.setMessage(message.getMessage());
                saved.setAuthor(message.getAuthor());
                saved.setChannelId(message.getChannelId());
                saved.setTimestamp(message.getTimestamp());
                saved.setSequenceNumber(sequentialNumber.getAndIncrement());

                messages.add(saved);

                return saved;
            } finally {
                lock.writeLock().unlock();
            }
        }
    }
}
