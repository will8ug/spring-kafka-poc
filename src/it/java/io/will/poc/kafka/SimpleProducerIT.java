package io.will.poc.kafka;

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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

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

    @Test
    public void testSimpleMessage() throws Exception {
        ResultActions resultActions = mvc.perform(
                post("/message")
                        .contentType(MediaType.TEXT_PLAIN_VALUE)
                        .content("simple message")
        );

        resultActions.andExpect(status().isNoContent());

        Path expectedOutputFile = Path.of("/tmp", "spring-kafk-poc-test.txt");
        int timeout = 0;
        while (timeout < 6) {
            try {
                System.out.println("sleeping for 5 seconds...");
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            if (Files.exists(expectedOutputFile) && Files.size(expectedOutputFile) > 0) {
                break;
            }
            timeout++;
        }

        assertEquals("simple message", Files.readString(expectedOutputFile));
        assertTrue(timeout < 6);
    }
}
