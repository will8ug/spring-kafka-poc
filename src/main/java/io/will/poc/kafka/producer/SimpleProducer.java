package io.will.poc.kafka.producer;

import io.will.poc.kafka.model.Greeting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

import static io.will.poc.kafka.config.KafkaTopicConfig.TOPIC_BASIC;
import static io.will.poc.kafka.config.KafkaTopicConfig.TOPIC_GREETING;

@Component
public class SimpleProducer {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private KafkaTemplate<String, Greeting> greetingKafkaTemplate;

    public void sendMessage(String msg) {
        kafkaTemplate.send(TOPIC_BASIC, msg);
    }

    public void sendMessageWithCallback(String message) {
        CompletableFuture<SendResult<String, String>> sendFuture = kafkaTemplate.send(TOPIC_BASIC, message);
        sendFuture.whenComplete((result, ex) -> {
            if (ex == null) {
                System.out.println("Sent message=[" + message + "] with offset=[" +
                        result.getRecordMetadata().offset() + "]");
            } else {
                System.out.println("Unable to send message=[" + message +
                        "] due to: " + ex.getMessage());
            }
        });
    }

    public void sendGreeting(Greeting greeting) {
        greetingKafkaTemplate.send(TOPIC_GREETING, greeting);
    }
}
