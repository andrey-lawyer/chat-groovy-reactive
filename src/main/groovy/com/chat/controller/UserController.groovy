package com.chat.controller;

import com.chat.dto.auth.UpdateProfileRequest;
import com.chat.dto.auth.UserCurrentResponseDto;
import com.chat.model.User;
import com.chat.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/user")
class UserController {

    private final UserService userService;

    @Autowired
    UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/current-user")
    Mono<UserCurrentResponseDto> checkAuthStatus() {
        return userService.isUserAuthenticated()
                .flatMap(isAuthenticated -> {
                    if (isAuthenticated) {
                        return userService.getCurrentUser();
                    } else {
                        return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED));
                    }
                });
    }

    @PutMapping("/update")
    Mono<User> updateProfile(@RequestBody UpdateProfileRequest updateProfileRequest) {
        return userService.updateUserProfile(
                updateProfileRequest.getNewUsername(),
                updateProfileRequest.getNewPassword()
        );
    }
}
