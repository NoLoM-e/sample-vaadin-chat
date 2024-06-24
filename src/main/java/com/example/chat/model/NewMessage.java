package com.example.chat.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
public class NewMessage {

    private String message;
    private String channelId;
    private String author;
    private Instant timestamp;
}
