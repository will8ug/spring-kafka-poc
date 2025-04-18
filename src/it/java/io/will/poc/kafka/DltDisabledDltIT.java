package io.will.poc.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.will.poc.kafka.config.KafkaTopicConfig;
import io.will.poc.kafka.consumer.DltDisabledDltConsumer;
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
import org.springframework.kafka.retrytopic.DltStrategy;
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
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = SpringKafkaPocApplication.class)
@AutoConfigureMockMvc
public class DltDisabledDltIT {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private AdminClient adminClient;

    @MockitoSpyBean
    private DltDisabledDltConsumer dltDisabledDltConsumer;

    @Test
    public void whenMainConsumerFails_thenDltConsumerDoesNotReceiveMessage() throws Exception {
        CountDownLatch mainTopicCountDownLatch = new CountDownLatch(1);
        CountDownLatch dltTopicCountDownLatch = new CountDownLatch(1);

        doAnswer(invocationOnMock -> {
            System.out.println("Coming in to a spying behavior of main consumer");
            mainTopicCountDownLatch.countDown();
            throw new Exception("Simulating error in main consumer");
        }).when(dltDisabledDltConsumer).handleRetryableGreeting(any(), any());

        doAnswer(invocationOnMock -> {
            System.out.println("Coming in to a spying behavior of dlt consumer [" + dltTopicCountDownLatch.getCount() + "]");
            dltTopicCountDownLatch.countDown();
            return null;
        }).when(dltDisabledDltConsumer).handleDltGreeting(any(), any());

        ResultActions resultActions = mvc.perform(
                post("/greeting-to-retry?strategy=" + DltStrategy.NO_DLT.name())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mockGreetingJson("greeting from IT at: " + LocalDateTime.now()))
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
        TopicPartition topicPartition = new TopicPartition(KafkaTopicConfig.TOPIC_RETRYABLE_DISABLED_DLT, 0);
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
