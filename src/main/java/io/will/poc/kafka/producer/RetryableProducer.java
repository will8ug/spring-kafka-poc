package io.will.poc.kafka.producer;

import io.will.poc.kafka.model.Greeting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import static io.will.poc.kafka.config.KafkaTopicConfig.TOPIC_RETRYABLE_FAIL_ON_ERROR;
import static io.will.poc.kafka.config.KafkaTopicConfig.TOPIC_RETRYABLE_RETRY_ON_ERROR;

@Component
public class RetryableProducer {
    @Autowired
    private KafkaTemplate<String, Greeting> greetingKafkaTemplate;

    public void sendGreetingToDltFailOnError(Greeting greeting) {
        greetingKafkaTemplate.send(TOPIC_RETRYABLE_FAIL_ON_ERROR, greeting);
    }

    public void sendGreetingToDltRetryOnError(Greeting greeting) {
        greetingKafkaTemplate.send(TOPIC_RETRYABLE_RETRY_ON_ERROR, greeting);
    }
}
