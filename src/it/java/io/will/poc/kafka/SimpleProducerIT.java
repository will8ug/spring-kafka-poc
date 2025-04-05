package io.will.poc.kafka;

import io.will.poc.kafka.domain.Message;
import io.will.poc.kafka.domain.MessageRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = SpringKafkaPocApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-it.yml")
public class SimpleProducerIT {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private MessageRepository messageRepository;

    @Test
    public void testSimpleMessage() throws Exception {
        ResultActions resultActions = mvc.perform(
                post("/message")
                        .contentType(MediaType.TEXT_PLAIN_VALUE)
                        .content("simple message")
        );

        resultActions.andExpect(status().isNoContent());

        Optional<Message> msg = waitUntilConsumerWorks();
        assertTrue(msg.isPresent());
    }

    private Optional<Message> waitUntilConsumerWorks() {
        int timeout = 30;
        while (timeout > 0) {
            waitFor5Seconds();
            timeout -= 5;

            try {
                Message msg = messageRepository.findByContent("simple message");
                System.out.println(msg);
                if (msg != null) {
                    return Optional.of(msg);
                }
            } catch (EntityNotFoundException e) {
                System.out.println(e.getMessage());
            }
        }
        return Optional.empty();
    }

    private static void waitFor5Seconds() {
        try {
            System.out.println("sleeping for 5 seconds...");
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
