package io.will.poc.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.will.poc.kafka.config.KafkaTopicConfig;
import io.will.poc.kafka.consumer.DltFailOnErrorConsumer;
import io.will.poc.kafka.model.Greeting;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DeleteRecordsResult;
import org.apache.kafka.clients.admin.RecordsToDelete;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = SpringKafkaPocApplication.class)
@AutoConfigureMockMvc
public class DltFailOnErrorIT {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private AdminClient adminClient;

    @MockitoSpyBean
    private DltFailOnErrorConsumer dltFailOnErrorConsumer;

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
                        .content(mockGreetingJson("greeting from IT - sad path - at: " + LocalDateTime.now()))
        );

        resultActions.andExpect(status().isNoContent());

        assertTrue(mainTopicCountDownLatch.await(15, TimeUnit.SECONDS));
        assertFalse(dltTopicCountDownLatch.await(15, TimeUnit.SECONDS));
        assertEquals(1, dltTopicCountDownLatch.getCount());
    }

    @AfterEach
    public void afterEach() throws ExecutionException, InterruptedException {
        System.out.println("Start to purge topics");

        Map<TopicPartition, RecordsToDelete> recordsToDelete = constructRecordsToDelete();
        System.out.println(recordsToDelete);
        DeleteRecordsResult deleteResult = adminClient.deleteRecords(recordsToDelete);
        deleteResult.all().get();

        System.out.println("End of purging topics");
    }

    private static Map<TopicPartition, RecordsToDelete> constructRecordsToDelete() {
        TopicPartition topicPartition = new TopicPartition(KafkaTopicConfig.TOPIC_RETRYABLE_FAIL_ON_ERROR, 0);
        RecordsToDelete recordsToDelete = RecordsToDelete.beforeOffset(-1);
        Map<TopicPartition, RecordsToDelete> recordsToDeleteMap = new HashMap<>();
        recordsToDeleteMap.put(topicPartition, recordsToDelete);
        return recordsToDeleteMap;
    }

    private static String mockGreetingJson(String rawMsg) throws JsonProcessingException {
        Greeting greeting = new Greeting(rawMsg, "Bob");
        return new ObjectMapper().writeValueAsString(greeting);
    }
}
