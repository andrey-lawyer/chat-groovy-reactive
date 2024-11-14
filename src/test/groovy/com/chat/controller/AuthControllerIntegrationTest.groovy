package com.chat.controller

import com.chat.dto.auth.UserRegisterRequestDto
import com.chat.dto.auth.UserRegisterResponseDto
import com.chat.exception.GlobalErrorAttributes
import com.chat.exception.UserAlreadyExistsException
import com.chat.service.AuthService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import static org.mockito.ArgumentMatchers.any


import static org.junit.jupiter.api.Assertions.assertNotNull;

import static org.mockito.Mockito.when;

@WebFluxTest(AuthController)
@ContextConfiguration(classes = [AuthController.class, GlobalErrorAttributes.class])
class AuthControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient

    @MockBean
    private AuthService authService

    protected WebTestClient getWebTestClient() {
        return webTestClient.mutateWith(SecurityMockServerConfigurers.csrf())
    }

    //   test admin
    protected void mockAuthenticatedUser() {
        webTestClient = webTestClient.mutateWith(SecurityMockServerConfigurers.mockUser("Steve Jobs").roles("ADMIN"))
    }

    @BeforeEach
    void setup() {
        mockAuthenticatedUser()
    }

    @Test
    void "authService should be injected"() {
        assertNotNull(authService, "AuthService should be injected by @MockBean");
    }

    @Test
    void "should return BAD_REQUEST status when request body is invalid"() {
        given:
        def invalidRequestDto = [
                username: "",
                password: "password"
        ]

        when:

        def result = getWebTestClient()
                .post()
                .uri("/api/auth/register")
                .bodyValue(invalidRequestDto)
                .exchange()

        then:

        result.expectStatus().isBadRequest()

                .expectBody()
                .jsonPath('$.error').isEqualTo('Validation failure')
                .jsonPath('$.fieldErrors[0].field').isEqualTo('username')
                .jsonPath('$.fieldErrors[0].message').isEqualTo('Username is mandatory')
    }

    @Test
    void shouldReturnCreatedStatusOnSuccessfulRegistration() {
        UserRegisterRequestDto requestDto = new UserRegisterRequestDto(username: "newUser", password: "password");
        UserRegisterResponseDto responseDto = new UserRegisterResponseDto("1", "newUser", "ADMIN");

        when(authService.register(any(UserRegisterRequestDto))).thenReturn(Mono.just(responseDto))

        // Executing the request via WebTestClient
        def result = getWebTestClient()
                .post()
                .uri("/api/auth/register")
                .bodyValue(requestDto)
                .exchange()

        // Check the status and body of the response
        result.expectStatus().isCreated()
                .expectBody()
                .jsonPath('$.id').isEqualTo("1")
                .jsonPath('$.username').isEqualTo("newUser")
                .jsonPath('$.role').isEqualTo("ADMIN")
    }


    @Test
    void "should return CONFLICT status when user already exists"() {
        def requestDto = new UserRegisterRequestDto(username: "existingUser", password: "password")

        // Mock the service to throw UserAlreadyExistsException when trying to register
        when(authService.register(any(UserRegisterRequestDto)))
                .thenReturn(Mono.error(new UserAlreadyExistsException()))

        // Execute the request and verify the conflict status and error message
        def result = getWebTestClient()
                .post()
                .uri("/api/auth/register")
                .bodyValue(requestDto)
                .exchange()

        result.expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody()
                .jsonPath('$.message').isEqualTo('Username already exists')
    }

}




