package io.will.poc.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.will.poc.kafka.domain.Message;
import io.will.poc.kafka.model.Greeting;
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
    private CommonUtils commonUtils;

    @Test
    public void testSimpleMessage() throws Exception {
        String message = "simple message from IT";
        ResultActions resultActions = mvc.perform(
                post("/message")
                        .contentType(MediaType.TEXT_PLAIN_VALUE)
                        .content(message)
        );

        resultActions.andExpect(status().isNoContent());

        Optional<Message> msg = commonUtils.waitUntilConsumerWorks(message);
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

        Optional<Message> msg = commonUtils.waitUntilConsumerWorks(message);
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

        Optional<Message> msg = commonUtils.waitUntilConsumerWorks(message, 15);
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

        Optional<Message> msg = commonUtils.waitUntilConsumerWorks(message);
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

        Optional<Message> msg = commonUtils.waitUntilConsumerWorks(rawMsg);
        assertTrue(msg.isPresent());
        assertEquals(Message.Type.GREETING, msg.get().getType());
    }
}
