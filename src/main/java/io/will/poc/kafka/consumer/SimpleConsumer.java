package io.will.poc.kafka.consumer;

import io.will.poc.kafka.domain.Message;
import io.will.poc.kafka.domain.MessageRepository;
import io.will.poc.kafka.model.Greeting;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import static io.will.poc.kafka.config.KafkaTopicConfig.*;

@Component
public class SimpleConsumer {
    private final Logger LOGGER = LoggerFactory.getLogger(SimpleConsumer.class);

    @Autowired
    private MessageRepository messageRepository;

    @KafkaListener(topics = TOPIC_WITH_FILTER, groupId = "foo", containerFactory = "filterKafkaListenerContainerFactory")
    @Transactional
    public void listenWithFilter(String message) {
        LOGGER.info("Received Message in filtered listener: {}", message);

        messageRepository.save(new Message(Message.Type.SIMPLE, message));
    }

    @KafkaListener(topics = TOPIC_BASIC)
    @Transactional
    public void listenWithHeaders(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition) {
        LOGGER.info("Received Message: [{}] from partition: {}", message, partition);

        messageRepository.save(new Message(Message.Type.SIMPLE, message));
    }

    @KafkaListener(topics = TOPIC_GREETING, containerFactory = "greetingKafkaListenerContainerFactory")
    @Transactional
    public void greetingListener(Greeting greeting) {
        LOGGER.info("Got a greeting: {}", greeting);

        messageRepository.save(new Message(Message.Type.GREETING, greeting.message()));
    }
}
