package io.will.poc.kafka.config;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaTopicConfig {
    private final Logger LOGGER = LoggerFactory.getLogger(KafkaTopicConfig.class);

    public static final String TOPIC_BASIC = "basic";
    public static final String TOPIC_GREETING = "greeting";
    public static final String TOPIC_MULTI_TYPE = "multi-type";
    public static final String TOPIC_WITH_FILTER = "filter";
    public static final String TOPIC_RETRYABLE_FAIL_ON_ERROR = "retryable-fail-on-error";
    public static final String TOPIC_RETRYABLE_RETRY_ON_ERROR = "retryable-retry-on-error";
    public static final String TOPIC_RETRYABLE_DISABLED_DLT = "retryable-disabled-dlt";

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic topicRetryableDisabledDlt() {
        LOGGER.info("Automatically creating a topic: {}", TOPIC_RETRYABLE_DISABLED_DLT);
        return new NewTopic(TOPIC_RETRYABLE_DISABLED_DLT, 1, (short) 1);
    }

    @Bean
    public AdminClient adminClient() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        return AdminClient.create(configs);
    }
}
