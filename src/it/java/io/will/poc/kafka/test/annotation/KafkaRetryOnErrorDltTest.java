package io.will.poc.kafka.test.annotation;

import io.will.poc.kafka.consumer.DltRetryOnErrorConsumer;
import io.will.poc.kafka.producer.RetryableProducer;
import io.will.poc.kafka.resource.RetryableProducerResource;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ContextConfiguration(classes = {DltRetryOnErrorConsumer.class, RetryableProducer.class, RetryableProducerResource.class})
@EnableAutoConfiguration
@ComponentScan(value = {"io.will.poc.kafka.config"})
@ActiveProfiles("dlt")
public @interface KafkaRetryOnErrorDltTest {
}
