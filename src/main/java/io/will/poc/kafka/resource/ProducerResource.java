package io.will.poc.kafka.resource;

import io.will.poc.kafka.model.Greeting;
import io.will.poc.kafka.producer.SimpleProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
public class ProducerResource {
    @Autowired
    private SimpleProducer simpleProducer;

    @GetMapping("/health")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void health() {
        System.out.println("GET /health");
    }

    @PostMapping(path = "/message")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void simpleMessage(@RequestBody String message) {
        System.out.println("/message: " + message);
        simpleProducer.sendMessage(message);
    }

    @PostMapping(path = "/greeting-message", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void greetingMessage(@RequestBody Greeting greeting) {
        System.out.println("/greeting-message: " + greeting);
        simpleProducer.sendGreeting(greeting);
    }
}
