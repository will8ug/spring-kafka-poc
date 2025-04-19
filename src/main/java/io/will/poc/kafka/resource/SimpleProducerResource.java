package io.will.poc.kafka.resource;

import io.will.poc.kafka.model.Greeting;
import io.will.poc.kafka.producer.SimpleProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import static io.will.poc.kafka.config.KafkaTopicConfig.TOPIC_WITH_FILTER;

@RestController
public class SimpleProducerResource {
    @Autowired
    private SimpleProducer simpleProducer;

    @GetMapping("/health")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void health() {
        System.out.println("GET /health");
    }

    @PostMapping(path = "/message", consumes = MediaType.TEXT_PLAIN_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void simpleMessage(@RequestBody String message,
                              @RequestParam(required = false) String topic) {
        System.out.printf("/message: request parameter 'topic'[%s] message[%s]%n", topic, message);
        if (TOPIC_WITH_FILTER.equalsIgnoreCase(topic)) {
            simpleProducer.sendMessageToFilterTopic(message);
        } else {
            simpleProducer.sendMessage(message);
        }
    }

    @PostMapping(path = "/message-call-back", consumes = MediaType.TEXT_PLAIN_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void simpleMessage(@RequestBody String message) {
        System.out.println("/message: " + message);
        simpleProducer.sendMessageWithCallback(message);
    }

    @PostMapping(path = "/greeting-message", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void greetingMessage(@RequestBody Greeting greeting) {
        System.out.println("/greeting-message: " + greeting);
        simpleProducer.sendGreeting(greeting);
    }
}
