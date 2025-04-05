package io.will.poc.kafka.consumer;

import io.will.poc.kafka.domain.Message;
import io.will.poc.kafka.domain.MessageRepository;
import io.will.poc.kafka.model.Greeting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import static io.will.poc.kafka.config.KafkaTopicConfig.*;

@Component
public class SimpleConsumer {
    @Autowired
    private MessageRepository messageRepository;

    @KafkaListener(topics = TOPIC_WITH_FILTER, groupId = "foo", containerFactory = "filterKafkaListenerContainerFactory")
    public void listenWithFilter(String message) {
        System.out.println("Received Message in filtered listener: " + message);

        messageRepository.save(new Message(Message.Type.SIMPLE, message));
    }

    @KafkaListener(topics = TOPIC_BASIC)
    public void listenWithHeaders(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition) {
        System.out.println("Received Message: [" + message + "] from partition: " + partition);

        messageRepository.save(new Message(Message.Type.SIMPLE, message));
    }

    @KafkaListener(topics = TOPIC_GREETING, containerFactory = "greetingKafkaListenerContainerFactory")
    public void greetingListener(Greeting greeting) {
        System.out.println("Got a greeting: " + greeting);

        messageRepository.save(new Message(Message.Type.GREETING, greeting.message()));
    }
}
