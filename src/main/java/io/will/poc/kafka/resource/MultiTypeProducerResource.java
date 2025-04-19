package io.will.poc.kafka.resource;

import io.will.poc.kafka.producer.MultiTypeProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MultiTypeProducerResource {
    @Autowired
    private MultiTypeProducer multiTypeProducer;

    @PostMapping("/multi-types")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void goMultiTypes() {
        System.out.println("POST /multi-types");
        multiTypeProducer.sendToMultiTypeTopic();
    }
}
