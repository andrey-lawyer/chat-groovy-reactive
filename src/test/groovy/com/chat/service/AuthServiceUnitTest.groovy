package com.chat.service

import com.chat.dto.auth.UserRegisterRequestDto
import com.chat.dto.auth.UserRegisterResponseDto
import com.chat.exception.UserAlreadyExistsException
import com.chat.model.User
import com.chat.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import reactor.core.publisher.Mono
import spock.lang.Specification
import spock.lang.Subject
import reactor.test.StepVerifier

class AuthServiceUnitTest extends Specification {

    def userRepository = Mock(UserRepository)
    def passwordEncoder = Mock(PasswordEncoder)

    @Subject
    def authService = new AuthService(userRepository, passwordEncoder)

    def "should return CREATED status on successful registration"() {
        given: "A new user and mocked repository behavior"
        def requestDto = new UserRegisterRequestDto(username: "newUser", password: "password")
        def savedUser = new User(id: "1", username: "newUser", password: "encodedPassword", role: "USER")
        def responseDto = new UserRegisterResponseDto(id: "1", username: "newUser", role: "USER")

        // Mocking repository to return empty when searching for an existing user
        userRepository.findByUsername("newUser") >> Mono.empty()  // Mocking empty Mono when no user is found
        userRepository.findByRole("ADMIN") >> Mono.empty()  // Mock empty Mono when no admin role is found

        // Mocking password encoding
        passwordEncoder.encode("password") >> "encodedPassword"

        // Mocking save to return the saved user
        userRepository.save(_) >> Mono.just(savedUser)

        when: "The register method is called"
        def result = authService.register(requestDto)

        then: "The user is registered and the response is correct"
        result.block() == responseDto
    }

    def "should return conflict status when user already exists"() {
        given: "An existing user in the repository"
        def requestDto = new UserRegisterRequestDto(username: "existingUser", password: "password")

        // Mocking repository to return an existing user when searching by username
        userRepository.findByUsername("existingUser") >> Mono.just(new User(id: "2", username: "existingUser", role: "User "))

        userRepository.findByRole("ADMIN") >> Mono.just(new User(id: "1", username: "admin", role: "ADMIN"))

        when: "Trying to register the user"
        def result = authService.register(requestDto)

        then: "It should throw a UserAlreadyExistsException"
        StepVerifier.create(result)
                .expectError(UserAlreadyExistsException)
                .verify()  // StepVerifier used to verify that the exception is thrown
    }
}
