package com.example.chat.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class CommonBeans {

    @Bean
    public Clock getClock() {
        return Clock.systemUTC();
    }
}
