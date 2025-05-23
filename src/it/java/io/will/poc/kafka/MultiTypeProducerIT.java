package io.will.poc.kafka;

import io.will.poc.kafka.domain.Message;
import io.will.poc.kafka.test.annotation.KafkaMultiTypeTest;
import io.will.poc.kafka.test.helper.TestRepositoryMonitor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@KafkaMultiTypeTest
@AutoConfigureMockMvc
public class MultiTypeProducerIT {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private TestRepositoryMonitor testRepositoryMonitor;

    @Test
    public void testMultiTypesMessage() throws Exception {
        ResultActions resultActions = mvc.perform(post("/multi-types"));

        resultActions.andExpect(status().isNoContent());

        Optional<Message> greetingMsg = testRepositoryMonitor.waitUntilConsumerWorks("greeting to multi-type topic");
        assertTrue(greetingMsg.isPresent());
        assertEquals(Message.Type.GREETING, greetingMsg.get().getType());

        Optional<Message> farewellMsg = testRepositoryMonitor.waitUntilConsumerWorks("farewell to multi-type topic");
        assertTrue(farewellMsg.isPresent());
        assertEquals(Message.Type.FAREWELL, farewellMsg.get().getType());

        Optional<Message> unknownMsg = testRepositoryMonitor.waitUntilConsumerWorks("static simple message to multi-type topic");
        assertTrue(unknownMsg.isPresent());
        assertEquals(Message.Type.SIMPLE, unknownMsg.get().getType());
    }
}
