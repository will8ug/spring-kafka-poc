package io.will.poc.kafka.test.suite;

import io.will.poc.kafka.HealthIT;
import io.will.poc.kafka.MultiTypeProducerIT;
import io.will.poc.kafka.SimpleProducerIT;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses(value = {HealthIT.class, SimpleProducerIT.class, MultiTypeProducerIT.class})
public class BasicCasesSuite {
}
