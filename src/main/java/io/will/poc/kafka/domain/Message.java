package io.will.poc.kafka.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.springframework.core.style.ToStringCreator;

import java.time.LocalDateTime;

@Entity
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Type type;
    private String content;
    private LocalDateTime createdTime;

    protected Message() {}

    public Message(Type type, String content) {
        this.type = type;
        this.content = content;
        this.createdTime = LocalDateTime.now();
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    @Override
    public String toString() {
        return new ToStringCreator(this)
                .append("Id", getId())
                .append("Type", getType())
                .append("Content", getContent())
                .append("createdTime", getCreatedTime())
                .toString();
    }

    public enum Type {
        SIMPLE,
        GREETING,
        FAREWELL
    }
}
