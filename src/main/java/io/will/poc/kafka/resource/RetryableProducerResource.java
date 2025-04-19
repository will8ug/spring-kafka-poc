package io.will.poc.kafka.resource;

import io.will.poc.kafka.model.Greeting;
import io.will.poc.kafka.producer.RetryableProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.web.bind.annotation.*;

@RestController
public class RetryableProducerResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(RetryableProducerResource.class);

    @Autowired
    private RetryableProducer retryableProducer;

    @PostMapping(path = "/greeting-to-retry", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void greetingWithRetry(@RequestBody Greeting greeting,
                                  @RequestParam(required = false) String strategy) {
        LOGGER.info("/greeting-to-retry: parameter 'strategy'[{}] message[{}}]", strategy, greeting);

        if (DltStrategy.ALWAYS_RETRY_ON_ERROR.name().equalsIgnoreCase(strategy)) {
            retryableProducer.sendGreetingToDltRetryOnError(greeting);
        } else if (DltStrategy.NO_DLT.name().equalsIgnoreCase(strategy)) {
            retryableProducer.sendGreetingToDisabledDlt(greeting);
        } else {
            retryableProducer.sendGreetingToDltFailOnError(greeting);
        }
    }
}
