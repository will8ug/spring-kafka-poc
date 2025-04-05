package io.will.poc.kafka.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
    Message findByContent(String content);
}
