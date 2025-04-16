package io.will.poc.kafka.consumer;

import io.will.poc.kafka.model.Greeting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import static io.will.poc.kafka.config.KafkaTopicConfig.TOPIC_RETRYABLE_RETRY_ON_ERROR;

@Component
public class DltRetryOnErrorConsumer {
    private final Logger LOGGER = LoggerFactory.getLogger(DltRetryOnErrorConsumer.class);

    @RetryableTopic(attempts = "1", kafkaTemplate = "retryableKafkaTemplate", dltStrategy = DltStrategy.ALWAYS_RETRY_ON_ERROR)
    @KafkaListener(topics = {TOPIC_RETRYABLE_RETRY_ON_ERROR}, containerFactory = "greetingKafkaListenerContainerFactory")
    public void handleRetryableGreeting(
            Greeting greeting,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        LOGGER.info("Received a greeting from topic[{}]: {}", topic, greeting);
    }

    @DltHandler
    public void handleDltGreeting(
            Greeting greeting,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        LOGGER.info("Received a greeting on DLT: topic: {} message: {}", topic, greeting);
    }
}
