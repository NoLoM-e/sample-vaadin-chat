package com.example.chat.repository;

import com.example.chat.model.Channel;
import com.example.chat.model.Message;
import com.example.chat.model.NewChannel;
import com.example.chat.repository.impl.InMemoryChannelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class InMemoryChannelRepositoryTest {

    private ChannelRepository repo;
    private MessageRepository messageRepoMock;

    @BeforeEach
    public void setUp() {
        messageRepoMock = Mockito.mock(MessageRepository.class);
        repo = new InMemoryChannelRepository(messageRepoMock);
    }

    @Test
    void repository_is_empty_at_first() {
        assertThat(repo.findAll()).isEmpty();
        assertThat(repo.exists("nonexistent")).isFalse();
        assertThat(repo.findById("nonexitent")).isEmpty();
    }

    @Test
    void repository_can_save_and_retrieve_channels() {
        var channel1 = repo.save(new NewChannel("channel1"));
        var channel2 = repo.save(new NewChannel("channel2"));

        assertThat(repo.findAll()).containsExactly(channel1, channel2);
        assertThat(repo.exists(channel1.getId())).isTrue();
        assertThat(repo.exists(channel2.getId())).isTrue();
        assertThat(repo.findById(channel1.getId())).contains(channel1);
        assertThat(repo.findById(channel2.getId())).contains(channel2);
    }

    @Test
    void channels_are_sorted_by_name() {
        var channel1 = repo.save(new NewChannel("channel1"));
        var channel2 = repo.save(new NewChannel("channel2"));

        assertThat(repo.findAll()).containsExactly(channel1, channel2);
    }

    @Test
    void latest_message_is_included_when_retrieving_channels() {
        var channel1 = repo.save(new NewChannel("channel1"));
        var message = new Message();
        message.setMessageId("messageId");
        message.setChannelId(channel1.getId());
        message.setSequenceNumber(1L);
        message.setAuthor("user");
        message.setTimestamp(Instant.now());
        message.setMessage("message");

        when(messageRepoMock.findLatest(channel1.getId(), 1)).thenReturn(List.of(message));
        assertThat(repo.findById(channel1.getId())).contains(new Channel(channel1.getId(), channel1.getName(), message));
    }
}