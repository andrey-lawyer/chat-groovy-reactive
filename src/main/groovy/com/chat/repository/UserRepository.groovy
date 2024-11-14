package com.chat.repository

import com.chat.model.User
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Mono

interface UserRepository extends ReactiveMongoRepository<User, String> {

    Mono<User> findByUsername(String username)

    Mono<User> findByRole(String role);

    def Mono<User> save(Object o)
}
