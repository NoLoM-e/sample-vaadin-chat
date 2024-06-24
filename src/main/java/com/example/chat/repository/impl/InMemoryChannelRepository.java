package com.example.chat.repository.impl;

import com.example.chat.model.Channel;
import com.example.chat.model.Message;
import com.example.chat.model.NewChannel;
import com.example.chat.repository.ChannelRepository;
import com.example.chat.repository.MessageRepository;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class InMemoryChannelRepository implements ChannelRepository {

    private MessageRepository messageRepository;
    private ConcurrentMap<String, Channel> storage = new ConcurrentHashMap<>();

    public InMemoryChannelRepository(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    private Channel getLatestMessageIfExists(Channel channel) {
        return messageRepository.findLatest(channel.getId(), 1).stream()
                .findFirst()
                .map(message -> new Channel(channel.getId(), channel.getName(), message))
                .orElse(channel);
    }

    @Override
    public List<Channel> findAll() {
        return storage.values().stream()
                .map(this::getLatestMessageIfExists)
                .sorted(Comparator.comparing(Channel::getName))
                .toList();
    }

    @Override
    public Channel save(NewChannel newChannel) {
        var uuid = UUID.randomUUID().toString();
        Channel channel = new Channel(uuid, newChannel.getName());
        storage.putIfAbsent(channel.getId(), channel);
        return channel;
    }

    @Override
    public Optional<Channel> findById(String channelId) {
        return Optional.ofNullable(storage.get(channelId))
                .map(this::getLatestMessageIfExists);
    }

    @Override
    public boolean exists(String channelId) {
        return storage.containsKey(channelId);
    }
}
