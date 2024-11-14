package com.chat.controller

import com.chat.dto.auth.UserRegisterRequestDto
import com.chat.dto.auth.UserRegisterResponseDto
import com.chat.exception.UserAlreadyExistsException
import com.chat.service.AuthService
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Specification



class AuthControllerUnitTest extends Specification {

    def authService = Mock(AuthService)
    def authController = new AuthController(authService)

    def "should return CREATED status on successful registration"() {
        given:
        def requestDto = new UserRegisterRequestDto(username: "newUser", password: "password")
        def responseDto = new UserRegisterResponseDto(id: "1", username: "newUser", role: "ADMIN")
        authService.register(_ as UserRegisterRequestDto) >> Mono.just(responseDto)

        when:
        def result = authController.register(requestDto)

        then:
        StepVerifier.create(result)
                .expectNextMatches { response ->
                    println("Type of response.id: ${response.id.getClass()}")
                    response.username == "newUser" && response.id == "1" && response.role == "ADMIN"
                }
                .verifyComplete()
    }

    def "should return conflict status when user already exists"() {
        given:
        def requestDto = new UserRegisterRequestDto(username: "existingUser", password: "password")
        authService.register(_ as UserRegisterRequestDto) >> Mono.error(new UserAlreadyExistsException())

        when:
        def result = authController.register(requestDto)

        then:
        StepVerifier.create(result)
                .expectError(UserAlreadyExistsException)
                .verify()
    }

}
