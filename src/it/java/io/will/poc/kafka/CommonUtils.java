package io.will.poc.kafka;

import io.will.poc.kafka.domain.Message;
import io.will.poc.kafka.domain.MessageRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
public class CommonUtils {
    @Autowired
    private MessageRepository messageRepository;

    public Optional<Message> waitUntilConsumerWorks(String expectedMessage, int timeout) {
        while (timeout > 0) {
            waitFor1Seconds();
            timeout -= 1;

            try {
                Message msg = messageRepository.findByContent(expectedMessage);
                System.out.printf("Got message from DB: %s%n", msg);
                if (msg != null) {
                    return Optional.of(msg);
                }
            } catch (EntityNotFoundException e) {
                // ignore and continue waiting
                System.out.println(e.getMessage());
            }
        }
        return Optional.empty();
    }

    public Optional<Message> waitUntilConsumerWorks(String expectedMessage) {
        return waitUntilConsumerWorks(expectedMessage, 30);
    }

    private static void waitFor1Seconds() {
        try {
            System.out.println("sleeping for 1 seconds...");
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
