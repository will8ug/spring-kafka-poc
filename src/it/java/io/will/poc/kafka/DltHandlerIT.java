package io.will.poc.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.will.poc.kafka.consumer.DltFailOnErrorConsumer;
import io.will.poc.kafka.consumer.DltRetryOnErrorConsumer;
import io.will.poc.kafka.model.Greeting;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = SpringKafkaPocApplication.class)
@AutoConfigureMockMvc
public class DltHandlerIT {
    @Autowired
    private MockMvc mvc;

    @MockitoSpyBean
    private DltFailOnErrorConsumer dltFailOnErrorConsumer;

    @MockitoSpyBean
    private DltRetryOnErrorConsumer dltRetryOnErrorConsumer;

    @Test
    public void whenMainConsumerSucceeds_thenNoDltMessage() throws Exception {
        CountDownLatch mainTopicCountDownLatch = new CountDownLatch(1);

        doAnswer(invocationOnMock -> {
            System.out.println("Coming in to a spying behavior");
            mainTopicCountDownLatch.countDown();
            return null;
        }).when(dltFailOnErrorConsumer).handleRetryableGreeting(any(), any());

        String jsonGreeting = mockGreetingJson("greeting message from IT - basic happy path");

        ResultActions resultActions = mvc.perform(
                post("/greeting-to-retry")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(jsonGreeting)
        );

        resultActions.andExpect(status().isNoContent());

        assertTrue(mainTopicCountDownLatch.await(15, TimeUnit.SECONDS));
        verify(dltFailOnErrorConsumer, never()).handleDltGreeting(any(), any());
    }

    @Test
    public void whenDltConsumerFails_thenDltProcessingStops() throws Exception {
        CountDownLatch mainTopicCountDownLatch = new CountDownLatch(1);
        CountDownLatch dltTopicCountDownLatch = new CountDownLatch(2);

        doAnswer(invocationOnMock -> {
            System.out.println("Coming in to a spying behavior of main consumer");
            mainTopicCountDownLatch.countDown();
            throw new Exception("Simulating error in main consumer");
        }).when(dltFailOnErrorConsumer).handleRetryableGreeting(any(), any());

        doAnswer(invocationOnMock -> {
            System.out.println("Coming in to a spying behavior of dlt consumer");
            dltTopicCountDownLatch.countDown();
            throw new Exception("Simulating error in dlt consumer");
        }).when(dltFailOnErrorConsumer).handleDltGreeting(any(), any());

        ResultActions resultActions = mvc.perform(
                post("/greeting-to-retry")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mockGreetingJson("greeting message from IT - sad path"))
        );

        resultActions.andExpect(status().isNoContent());

        assertTrue(mainTopicCountDownLatch.await(15, TimeUnit.SECONDS));
        assertFalse(dltTopicCountDownLatch.await(15, TimeUnit.SECONDS));
        assertEquals(1, dltTopicCountDownLatch.getCount());
    }

    @Test
    public void whenDltConsumerFails_thenDltConsumerRetriesMessage() throws Exception {
        CountDownLatch mainTopicCountDownLatch = new CountDownLatch(1);
        CountDownLatch dltTopicCountDownLatch = new CountDownLatch(3);

        doAnswer(invocationOnMock -> {
            System.out.println("Coming in to a spying behavior of main consumer");
            mainTopicCountDownLatch.countDown();
            throw new Exception("Simulating error in main consumer");
        }).when(dltRetryOnErrorConsumer).handleRetryableGreeting(any(), any());

        doAnswer(invocationOnMock -> {
            System.out.println("Coming in to a spying behavior of dlt consumer [" + dltTopicCountDownLatch.getCount() + "]");
            dltTopicCountDownLatch.countDown();
            throw new Exception("Simulating error in dlt consumer");
        }).when(dltRetryOnErrorConsumer).handleDltGreeting(any(), any());

        ResultActions resultActions = mvc.perform(
                post("/greeting-to-retry?strategy=" + DltStrategy.ALWAYS_RETRY_ON_ERROR.name())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mockGreetingJson("greeting message from IT - basic case"))
        );

        resultActions.andExpect(status().isNoContent());

        assertTrue(mainTopicCountDownLatch.await(15, TimeUnit.SECONDS));
        assertTrue(dltTopicCountDownLatch.await(15, TimeUnit.SECONDS));
        assertEquals(0, dltTopicCountDownLatch.getCount());
    }

    // TODO: purge records in failure related topics for tests robustness

    private static String mockGreetingJson(String rawMsg) throws JsonProcessingException {
        Greeting greeting = new Greeting(rawMsg, "Bob");
        return new ObjectMapper().writeValueAsString(greeting);
    }
}
