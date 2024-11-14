package com.chat.model

import lombok.Data
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "messages")
@Data
class Message {
    @Id
    String id
    String username
    String content
    Instant timestamp = Instant.now()
}

