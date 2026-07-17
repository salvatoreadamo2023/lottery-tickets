package com.lottery.tickets;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LotteryBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(LotteryBackendApplication.class, args);
    }
}