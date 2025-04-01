package io.will.poc.kafka.consumer;

import io.will.poc.kafka.model.Farewell;
import io.will.poc.kafka.model.Greeting;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static io.will.poc.kafka.config.KafkaTopicConfig.TOPIC_MULTI_TYPE;

@Component
@KafkaListener(groupId = "multiTypeGroup", topics = TOPIC_MULTI_TYPE, containerFactory = "multiTypeKafkaListenerContainerFactory")
public class MultiTypeConsumer {
    @KafkaHandler
    public void handleGreeting(Greeting greeting) {
        System.out.println("Received a greeting in MultiTypeConsumer: " + greeting);
    }

    @KafkaHandler
    public void handleFarewell(Farewell farewell) {
        System.out.println("Received a farewell in MultiTypeConsumer: " + farewell);
    }

    @KafkaHandler(isDefault = true)
    public void handleDefault(Object object) {
        System.out.println("Received an unknown object in MultiTypeConsumer: " + object);
    }
}


