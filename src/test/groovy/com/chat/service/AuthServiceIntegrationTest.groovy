package com.chat.service

import com.chat.dto.auth.UserRegisterRequestDto
import com.chat.dto.auth.UserRegisterResponseDto
import com.chat.exception.UserAlreadyExistsException
import com.chat.model.User
import com.chat.repository.UserRepository
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertNotNull

import static org.mockito.Mockito.when

@WebFluxTest(AuthService)
@ContextConfiguration(classes = [AuthService.class, PasswordEncoder.class])
class AuthServiceIntegrationTest {

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthService authService;

    @Autowired
    private WebTestClient webTestClient;



    @BeforeEach
    void setup() {

        this.webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:8080").build();
    }
    @Test
    void "userRepository should be injected"() {
        assertNotNull(userRepository, "UserRepository should be injected by @MockBean");
    }

    @Test
    void "authService should be injected"() {
        assertNotNull(authService, "AuthService should be injected by @Autowired");
    }


    @Test
    void shouldReturnConflictStatusWhenUserAlreadyExists() {
        // Arrange: setting up existing user
        // Preparing a request DTO with the username "existingUser" and password "password" to simulate the user registration attempt
        UserRegisterRequestDto requestDto = new UserRegisterRequestDto(username: "existingUser", password: "password");

        // Mock the response for findByUsername so that it returns a Mono with an existing user
        // Creating a mock user object that represents an already existing user in the system
        User existingUser = new User(id: "2", username: "existingUser", password: "encodedPassword", role: "User");

        // When the repository is queried with the username "existingUser", return a Mono containing the existing user
        when(userRepository.findByUsername("existingUser"))
                .thenReturn(Mono.just(existingUser));  // Returning a Mono that contains the already existing user

        // Mock findByRole for "ADMIN", so it returns Mono.empty()
        // Simulating the case where there is no user with the "ADMIN" role in the database
        when(userRepository.findByRole("ADMIN"))
                .thenReturn(Mono.empty());  // Returning an empty Mono, simulating no "ADMIN" user found

        // Act: calling the register method
        // Call the register method in the AuthService, which attempts to register the user
        Mono<UserRegisterResponseDto> result = authService.register(requestDto);

        // Assert: expecting UserAlreadyExistsException
        // Using StepVerifier to validate the behavior of the result
        // Expecting that the method will throw a UserAlreadyExistsException due to the existing user
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof UserAlreadyExistsException)  // Asserting the exception type
                .verify();  // Verifying the result
    }


    @Test
    void shouldReturnCreatedStatusOnSuccessfulRegistration() {
        // Arrange: setting up new user and admin check
        UserRegisterRequestDto requestDto = new UserRegisterRequestDto(username:"newUser", password:"password");
        User savedUser = new User(id: "1", username:"newUser", password:"encodedPassword",role: "USER");
        UserRegisterResponseDto responseDto = new UserRegisterResponseDto("1", "newUser", "USER");

        when(userRepository.findByUsername(requestDto.getUsername())).thenReturn(Mono.empty());
        when(userRepository.findByRole("ADMIN")).thenReturn(Mono.empty());
        when(passwordEncoder.encode(requestDto.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(savedUser));

        // Act: calling the register method
        Mono<UserRegisterResponseDto> result = authService.register(requestDto);

        // Assert: verifying successful registration and response
        StepVerifier.create(result)
                .expectNext(responseDto)
                .verifyComplete();
    }
    @Test
    void shouldReturnAdminWhenFirstUserIsRegistered() {
        // Arrange: first user registered should become admin
        UserRegisterRequestDto requestDto = new UserRegisterRequestDto(username:"firstUser", password:"password");
        User savedUser = new User( id: "1", username: "firstUser", password: "encodedPassword",role: "ADMIN");
        UserRegisterResponseDto responseDto = new UserRegisterResponseDto("1", "firstUser", "ADMIN");

        when(userRepository.findByUsername(requestDto.getUsername())).thenReturn(Mono.empty());
        when(userRepository.findByRole("ADMIN")).thenReturn(Mono.empty());
        when(passwordEncoder.encode(requestDto.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(savedUser));

        // Act: calling the register method
        Mono<UserRegisterResponseDto> result = authService.register(requestDto);

        // Assert: verifying the user is created as an admin
        StepVerifier.create(result)
                .expectNext(responseDto)
                .verifyComplete();
    }
}




