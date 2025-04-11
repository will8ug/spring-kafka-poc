package io.will.poc.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.will.poc.kafka.consumer.RetryableConsumer;
import io.will.poc.kafka.model.Greeting;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = SpringKafkaPocApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-it.yml")
public class DltHandlerIT {
    @Autowired
    private MockMvc mvc;

    @MockitoSpyBean
    private RetryableConsumer retryableConsumer;

    @Test
    public void testMainConsumerSucceeds_noDltMessage() throws Exception {
        CountDownLatch mainTopicCountDownLatch = new CountDownLatch(1);

        doAnswer(invocationOnMock -> {
            System.out.println("Coming in to a spying behavior");
            mainTopicCountDownLatch.countDown();
            return null;
        }).when(retryableConsumer).handleRetryableGreeting(any(), any());

        String jsonGreeting = mockGreetingJson("greeting message from IT");

        ResultActions resultActions = mvc.perform(
                post("/greeting-to-retry")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(jsonGreeting)
        );

        resultActions.andExpect(status().isNoContent());

        assertTrue(mainTopicCountDownLatch.await(15, TimeUnit.SECONDS));
        verify(retryableConsumer, never()).handleDltGreeting(any(), any());
    }

    private static String mockGreetingJson(String rawMsg) throws JsonProcessingException {
        Greeting greeting = new Greeting(rawMsg, "Bob");
        return new ObjectMapper().writeValueAsString(greeting);
    }
}
