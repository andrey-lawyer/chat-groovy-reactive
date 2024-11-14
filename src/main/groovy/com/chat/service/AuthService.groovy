package com.chat.service

import com.chat.dto.auth.UserRegisterRequestDto
import com.chat.dto.auth.UserRegisterResponseDto
import com.chat.exception.UserAlreadyExistsException
import com.chat.model.User
import com.chat.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono


@Service
class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    Mono<UserRegisterResponseDto> register(UserRegisterRequestDto userDto) {
        return userRepository.findByUsername(userDto.getUsername())
                .flatMap(existingUser -> Mono.error(new UserAlreadyExistsException()))
                .switchIfEmpty(userRepository.findByRole("ADMIN")
                        .flatMap(admin -> {
                            User user = new User();
                            user.setUsername(userDto.getUsername());
                            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
                            user.setRole("USER");

                            return userRepository.save(user)
                                    .map(savedUser -> new UserRegisterResponseDto(savedUser.getId(), savedUser.getUsername(), savedUser.getRole()));
                        })
                        .switchIfEmpty(Mono.defer(() -> {
                            User user = new User();
                            user.setUsername(userDto.getUsername());
                            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
                            user.setRole("ADMIN");

                            return userRepository.save(user)
                                    .map(savedUser -> new UserRegisterResponseDto(savedUser.getId(), savedUser.getUsername(), savedUser.getRole()));
                        }))
                ) as Mono<UserRegisterResponseDto>;
    }
}

