package com.chat.controller;

import com.chat.dto.auth.UserRegisterRequestDto;
import com.chat.dto.auth.UserRegisterResponseDto;
import com.chat.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
class AuthController {

    private final AuthService authService;

    @Autowired
    AuthController(AuthService authService) {
        this.authService = authService;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/register")
    Mono<UserRegisterResponseDto> register(@Valid @RequestBody UserRegisterRequestDto userDto) {
        return authService.register(userDto);
    }
}
