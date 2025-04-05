package io.will.poc.kafka.producer;

import io.will.poc.kafka.model.Farewell;
import io.will.poc.kafka.model.Greeting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import static io.will.poc.kafka.config.KafkaTopicConfig.TOPIC_MULTI_TYPE;

@Component
public class MultiTypeProducer {
    @Autowired
    private KafkaTemplate<String, Object> multiTypeKafkaTemplate;

    public void sendToMultiTypeTopic() {
        multiTypeKafkaTemplate.send(TOPIC_MULTI_TYPE, new Greeting("greeting to multi-type topic", "Alice"));
        multiTypeKafkaTemplate.send(TOPIC_MULTI_TYPE, new Farewell("farewell to multi-type topic", 10));
        multiTypeKafkaTemplate.send(TOPIC_MULTI_TYPE, "static simple message to multi-type topic");
    }
}
