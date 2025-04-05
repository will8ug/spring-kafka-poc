package io.will.poc.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.will.poc.kafka.domain.Message;
import io.will.poc.kafka.domain.MessageRepository;
import io.will.poc.kafka.model.Greeting;
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

import static io.will.poc.kafka.config.KafkaTopicConfig.TOPIC_WITH_FILTER;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
        String message = "simple message from IT";
        ResultActions resultActions = mvc.perform(
                post("/message")
                        .contentType(MediaType.TEXT_PLAIN_VALUE)
                        .content(message)
        );

        resultActions.andExpect(status().isNoContent());

        Optional<Message> msg = waitUntilConsumerWorks(message);
        assertTrue(msg.isPresent());
        assertEquals(Message.Type.SIMPLE, msg.get().getType());
    }

    @Test
    public void testSimpleMessage_withFilter_notFilterOut() throws Exception {
        String message = "simple message passing through the filter";
        ResultActions resultActions = mvc.perform(
                post("/message?topic=" + TOPIC_WITH_FILTER)
                        .contentType(MediaType.TEXT_PLAIN_VALUE)
                        .content(message)
        );

        resultActions.andExpect(status().isNoContent());

        Optional<Message> msg = waitUntilConsumerWorks(message);
        assertTrue(msg.isPresent());
        assertEquals(Message.Type.SIMPLE, msg.get().getType());
    }

    @Test
    public void testSimpleMessage_withFilter_filterOut() throws Exception {
        String message = "Hello World!";
        ResultActions resultActions = mvc.perform(
                post("/message?topic=" + TOPIC_WITH_FILTER)
                        .contentType(MediaType.TEXT_PLAIN_VALUE)
                        .content(message)
        );

        resultActions.andExpect(status().isNoContent());

        Optional<Message> msg = waitUntilConsumerWorks(message, 15);
        assertTrue(msg.isEmpty());
    }

    @Test
    public void testSimpleMessage_withCallback() throws Exception {
        String message = "message to callback from IT";
        ResultActions resultActions = mvc.perform(
                post("/message-call-back")
                        .contentType(MediaType.TEXT_PLAIN_VALUE)
                        .content(message)
        );

        resultActions.andExpect(status().isNoContent());

        Optional<Message> msg = waitUntilConsumerWorks(message);
        assertTrue(msg.isPresent());
        assertEquals(Message.Type.SIMPLE, msg.get().getType());
    }

    @Test
    public void testGreetingMessage() throws Exception {
        String rawMsg = "greeting message from IT";
        Greeting greeting = new Greeting(rawMsg, "Bob");
        String jsonGreeting = new ObjectMapper().writeValueAsString(greeting);
        System.out.println(jsonGreeting);

        ResultActions resultActions = mvc.perform(
                post("/greeting-message")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(jsonGreeting)
        );

        resultActions.andExpect(status().isNoContent());

        Optional<Message> msg = waitUntilConsumerWorks(rawMsg);
        assertTrue(msg.isPresent());
        assertEquals(Message.Type.GREETING, msg.get().getType());
    }

    @Test
    public void testMultiTypesMessage() throws Exception {
        ResultActions resultActions = mvc.perform(post("/multi-types"));

        resultActions.andExpect(status().isNoContent());

        Optional<Message> greetingMsg = waitUntilConsumerWorks("greeting to multi-type topic");
        assertTrue(greetingMsg.isPresent());
        assertEquals(Message.Type.GREETING, greetingMsg.get().getType());

        Optional<Message> farewellMsg = waitUntilConsumerWorks("farewell to multi-type topic");
        assertTrue(farewellMsg.isPresent());
        assertEquals(Message.Type.FAREWELL, farewellMsg.get().getType());

        Optional<Message> unknownMsg = waitUntilConsumerWorks("static simple message to multi-type topic");
        assertTrue(unknownMsg.isPresent());
        assertEquals(Message.Type.SIMPLE, unknownMsg.get().getType());
    }

    private Optional<Message> waitUntilConsumerWorks(String expectedMessage, int timeout) {
        while (timeout > 0) {
            waitFor1Seconds();
            timeout -= 1;

            try {
                Message msg = messageRepository.findByContent(expectedMessage);
                System.out.println(msg);
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

    private Optional<Message> waitUntilConsumerWorks(String expectedMessage) {
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
