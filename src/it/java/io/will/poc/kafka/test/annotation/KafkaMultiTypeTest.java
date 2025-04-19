package io.will.poc.kafka.test.annotation;

import io.will.poc.kafka.consumer.MultiTypeConsumer;
import io.will.poc.kafka.producer.MultiTypeProducer;
import io.will.poc.kafka.resource.MultiTypeProducerResource;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ContextConfiguration(classes = {MultiTypeConsumer.class, MultiTypeProducer.class, MultiTypeProducerResource.class})
@EnableAutoConfiguration
@ComponentScan(value = {"io.will.poc.kafka.config", "io.will.poc.kafka.domain", "io.will.poc.kafka.test.helper"})
@ActiveProfiles("simple")
public @interface KafkaMultiTypeTest {
}
