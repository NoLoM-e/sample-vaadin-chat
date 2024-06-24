package com.example.chat.exception;

public class InvalidChannelException extends IllegalArgumentException {
    public InvalidChannelException(String message) {
        super(message);
    }
}
