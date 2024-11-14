package com.chat.repository

import com.chat.model.Message
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Mono

interface  MessageRepository extends ReactiveMongoRepository<Message, String> {

    Mono<Void> deleteById(String id)
}
