package com.example.chat.repository;

import com.example.chat.model.Channel;
import com.example.chat.model.NewChannel;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChannelRepository {

    List<Channel> findAll();

    Channel save(NewChannel newChannel);

    Optional<Channel> findById(String channelId);

    boolean exists(String channelId);
}