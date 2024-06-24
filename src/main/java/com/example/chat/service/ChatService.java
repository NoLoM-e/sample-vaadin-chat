package com.example.chat.service;

import com.example.chat.exception.InvalidChannelException;
import com.example.chat.model.Channel;
import com.example.chat.model.Message;
import com.example.chat.model.NewChannel;
import com.example.chat.model.NewMessage;
import com.example.chat.repository.ChannelRepository;
import com.example.chat.repository.MessageRepository;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ChatService {

    private MessageRepository messageRepository;
    private ChannelRepository channelRepository;
    private Clock clock;
    private Sinks.Many<Message> messageSink = Sinks.many().multicast().directBestEffort();
    private static final Duration BUFFER_DURATION = Duration.ofMillis(300);

    public ChatService(MessageRepository messageRepository, ChannelRepository channelRepository, Clock clock) {
        this.messageRepository = messageRepository;
        this.channelRepository = channelRepository;
        this.clock = clock;
        generateTestData();
    }

    private void generateTestData() {
        String[] chatChannels = {
                "TechTalks Central",
                "Mindful Mornings",
                "Global Gourmet Guild",
                "Fitness Frontiers",
                "Bookworm Bungalow",
                "Creative Corner",
                "Eco Enthusiasts",
                "History Huddle",
                "Music Mavens",
                "Travel Trekkers",
                "Gamer's Grind",
                "Pet Parade",
                "Fashion Forward",
                "Science Sphere",
                "Artists' Alley",
                "Movie Maniacs",
                "Entrepreneur Exchange",
                "Health Hub",
                "DIY Den",
                "Language Labyrinth"
        };
        for (String channelName : chatChannels) {
            var channel = createChannel(channelName);
            log.info("Created channel: {} (http://localhost:8080/channel/{})", channel.getName(), channel.getId());
        }
    }

    public Channel createChannel(String name) {
        return channelRepository.save(new NewChannel(name));
    }

    public List<Channel> getAllChannels() {
        return channelRepository.findAll();
    }

    public Optional<Channel> getChannelById(String channelId) {
        return channelRepository.findById(channelId);
    }

    public List<Message> getLatestMessages(String channelId, int fetchMax, @Nullable String lastSeenMessageId) {
        return messageRepository.findLatest(channelId, fetchMax, lastSeenMessageId);
    }

    public Flux<List<Message>> liveMessages(String channelId) {
        if (!channelRepository.exists(channelId)) {
            throw new InvalidChannelException("Cannot post to channel %s".formatted(channelId));
        }

        return messageSink.asFlux().filter(message -> message.getChannelId().equals(channelId)).buffer(BUFFER_DURATION);
    }

    public void postMessage(String channelId, String message) {
        if (!channelRepository.exists(channelId)) {
            throw new InvalidChannelException("Cannot post to channel %s".formatted(channelId));
        }

        String author = "John Doe";
        var posted = new NewMessage(message, channelId, author, clock.instant());
        var saved = messageRepository.save(posted);
        var result = messageSink.tryEmitNext(saved);
        if (result.isFailure()) {
            log.error("Failed to post message to channelId {} [ Message : {} ]", message, channelId);
        }
    }
}
