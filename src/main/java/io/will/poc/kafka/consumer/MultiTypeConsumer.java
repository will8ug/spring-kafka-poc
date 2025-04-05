package io.will.poc.kafka.consumer;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.will.poc.kafka.domain.Message;
import io.will.poc.kafka.domain.MessageRepository;
import io.will.poc.kafka.model.Farewell;
import io.will.poc.kafka.model.Greeting;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.mapping.AbstractJavaTypeMapper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

import static io.will.poc.kafka.config.KafkaTopicConfig.TOPIC_MULTI_TYPE;

@Component
@KafkaListener(groupId = "multiTypeGroup", topics = TOPIC_MULTI_TYPE, containerFactory = "multiTypeKafkaListenerContainerFactory")
public class MultiTypeConsumer {
    @Autowired
    private MessageRepository messageRepository;

    @KafkaHandler
    public void handleGreeting(Greeting greeting) {
        System.out.println("Received a greeting in MultiTypeConsumer: " + greeting);

        messageRepository.save(new Message(Message.Type.GREETING, greeting.message()));
    }

    @KafkaHandler
    public void handleFarewell(Farewell farewell) {
        System.out.println("Received a farewell in MultiTypeConsumer: " + farewell);

        messageRepository.save(new Message(Message.Type.FAREWELL, farewell.message()));
    }

    @KafkaHandler(isDefault = true)
    public void handleDefault(Object object) {
        System.out.println("Received an unknown object in MultiTypeConsumer: " + object);

        messageRepository.save(new Message(Message.Type.SIMPLE,
                object == null ? "null" : object.toString()));
    }

    /*
      No use for now, just a piece of PoC code.
     */
    public static JavaType selectType(byte[] data, Headers headers) {
        Header header = headers.lastHeader(AbstractJavaTypeMapper.DEFAULT_CLASSID_FIELD_NAME);
        String classId = new String(header.value(), StandardCharsets.UTF_8);
        switch (classId) {
            case "greeting" -> {
                System.out.println("Recognized a Greeting message.");
                return TypeFactory.defaultInstance().constructType(Greeting.class);
            }
            case "farewell" -> {
                System.out.println("Recognized a Farewell message.");
                return TypeFactory.defaultInstance().constructType(Farewell.class);
            }
            default -> {
                System.out.println("Recognized a message of unknown type.");
                return TypeFactory.defaultInstance().constructType(Object.class);
            }
        }
    }
}


