package io.will.poc.kafka.consumer;

import io.will.poc.kafka.model.Greeting;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import static io.will.poc.kafka.config.KafkaTopicConfig.TOPIC_BASIC;
import static io.will.poc.kafka.config.KafkaTopicConfig.TOPIC_GREETING;

@Component
public class SimpleConsumer {

    @KafkaListener(topics = TOPIC_BASIC, groupId = "foo", containerFactory = "filterKafkaListenerContainerFactory")
    public void listenWithFilter(String message) {
        System.out.println("Received Message in filtered listener: " + message);
    }

//    @KafkaListener(topics = "topic1, topic2", groupId = "foo")
//    public void listenGroupFoo(String message) {
//        System.out.println("Received Message in group foo (topic1, topic2): " + message);
//    }

    @KafkaListener(topics = TOPIC_BASIC)
    public void listenWithHeaders(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition) {
        System.out.println("Received Message: [" + message + "] from partition: " + partition);
    }

//    @KafkaListener(
//            topicPartitions = @TopicPartition(topic = TOPIC_BASIC, partitions = {"0", "1"}),
//            containerFactory = "partitionsKafkaListenerContainerFactory")
//    public void listenToPartition(
//            @Payload String message,
//            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition) {
//        System.out.println("Received Message: [" + message + "] from partition: " + partition);
//    }

    @KafkaListener(topics = TOPIC_GREETING, containerFactory = "greetingKafkaListenerContainerFactory")
    public void greetingListener(Greeting greeting) {
        System.out.println("Got a greeting: " + greeting);
    }
}
