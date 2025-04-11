package io.will.poc.kafka.producer;

import io.will.poc.kafka.model.Greeting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import static io.will.poc.kafka.config.KafkaTopicConfig.TOPIC_RETRYABLE;

@Component
public class RetryableProducer {
    @Autowired
    private KafkaTemplate<String, Greeting> greetingKafkaTemplate;

    public void sendGreetingToRetry(Greeting greeting) {
        greetingKafkaTemplate.send(TOPIC_RETRYABLE, greeting);
    }
}
