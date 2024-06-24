package com.example.chat.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class Message {

    private String messageId;
    private String channelId;
    private String author;
    private Long sequenceNumber;
    private String message;
    private Instant timestamp;
}
