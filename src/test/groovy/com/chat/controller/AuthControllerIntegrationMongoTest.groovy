package com.chat.controller;

import com.chat.AbstractIntegrationTest;
import com.chat.dto.auth.UserRegisterRequestDto;
import com.chat.service.AuthService

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType

class AuthControllerIntegrationMongoTest extends AbstractIntegrationTest {

    @Autowired
    private AuthService authService;

    @Test
    void shouldReturnCreatedStatusOnSuccessfulRegistrationAndRoleAdmin() {


        UserRegisterRequestDto requestDto = new UserRegisterRequestDto(username: "firstUser", password: "password");

        webTestClient.post()
                .uri("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(requestDto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath('$.id').exists()
                 .jsonPath('$.id').value { id -> assert id.toString().matches('^[a-fA-F0-9]{24}$') }
                .jsonPath('$.username').isEqualTo("firstUser")
                .jsonPath('$.role').isEqualTo("ADMIN")
    }



    @Test
    void shouldReturnBadRequestWhenRequestBodyIsInvalid() {
        UserRegisterRequestDto invalidRequestDto = new UserRegisterRequestDto(username: "",password:"password");

        webTestClient.post()
                .uri("/api/auth/register")
                .bodyValue(invalidRequestDto)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath('$.error').isEqualTo("Validation failure")
                .jsonPath('$.fieldErrors[0].field').isEqualTo("username")
                .jsonPath('$.fieldErrors[0].message').isEqualTo("Username is mandatory");
    }
}