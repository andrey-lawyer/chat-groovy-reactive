package com.chat.service

import com.chat.model.Message;
import com.chat.repository.MessageRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;

@Service
class MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);

    private final MessageRepository messageRepository;
    private final DateTimeFormatter formatter;
    private final ObjectMapper objectMapper;

    MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
        this.formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("UTC"));
        this.objectMapper = new ObjectMapper();
    }

    Flux<String> loadMessageHistory() {
        return messageRepository.findAll()
                .map(this::createMessageJson)
                .filter(Objects::nonNull) as Flux<String>;
    }

    Mono<String> handleSaveMessage(String text, String username) {
        Message message = new Message(username:username, content: text, timestamp: Instant.now());
        return messageRepository.save(message)
                .map(this::createMessageJson);
    }

    Mono<String> handleDeleteMessage(String messageId, String username, boolean isAdmin) {
        if (isAdmin) {
            return messageRepository.findById(messageId)
                    .flatMap(message -> messageRepository.deleteById(messageId)
                            .then(Mono.fromCallable(() -> {
                                logger.info("Message deleted by admin: " + username);
                                return objectMapper.writeValueAsString(createMessageData(message, true));
                            }))
                    )
                    .switchIfEmpty(Mono.just("Error: Message not found"));
        } else {
            logger.warn("Insufficient permissions for user: " + username);
            return Mono.just("Error: Insufficient permissions to delete message");
        }
    }

    private String createMessageJson(Message message) {
        try {
            return objectMapper.writeValueAsString(createMessageData(message, false));
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize message to JSON: " + e.getMessage());
            return null;
        }
    }

    Map<String, Object> parseMessage(String content) {
        try {
            return new ObjectMapper().readValue(content, Map.class);
        } catch (Exception e) {
            logger.error("Failed to parse message: " + e.getMessage());
            return null;
        }
    }


    private Map<String, Object> createMessageData(Message message, boolean isDeleted) {
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("id", message.getId());
        messageData.put("timestamp", formatter.format(message.getTimestamp()));
        messageData.put("username", message.getUsername());
        messageData.put("content", message.getContent());
        messageData.put("isDeleted", isDeleted);
        return messageData;
    }


}



