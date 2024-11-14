package com.chat.service


import com.chat.dto.auth.UserCurrentResponseDto
import com.chat.model.User
import com.chat.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;

@Service
class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

       @Autowired
       UserService(UserRepository userRepository) {
           this.userRepository = userRepository;
           this.passwordEncoder = passwordEncoder;
        }

    Mono<UserCurrentResponseDto> getCurrentUser() {
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> {
                    Authentication authentication = context.getAuthentication();
                    print(authentication)
                    String username = authentication.getName();
                    String role = authentication.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .map(auth -> auth.replace("ROLE_", ""))
                            .findFirst()
                            .orElse("USER");

                    return new UserCurrentResponseDto(username, role);
                });
    }


    Mono<Boolean> isUserAuthenticated() {
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> context.getAuthentication().isAuthenticated())
                .defaultIfEmpty(false);
    }



    Mono<User> updateUserProfile(String newUsername, String newPassword) {
        return ReactiveSecurityContextHolder.getContext()
                .flatMap(context -> {
                    UserDetails userDetails = (UserDetails) context.getAuthentication().getPrincipal();
                    String currentUsername = userDetails.getUsername();

                    return userRepository.findByUsername(currentUsername)
                            .flatMap(currentUser -> {

                                if (newUsername != null && !newUsername.isBlank() && newUsername != currentUsername) {
                                    return userRepository.findByUsername(newUsername)
                                            .flatMap(existingUser -> Mono.error(new RuntimeException("Username already exists.")))
                                            .switchIfEmpty(updateUserFields(currentUser, newUsername, newPassword));
                                } else {
                                    return updateUserFields(currentUser, newUsername, newPassword);
                                }
                            });
                }) as Mono<User>;
    }

    private Mono<User> updateUserFields(User currentUser, String newUsername, String newPassword) {
        if (newUsername != null && !newUsername.isBlank()) {
            currentUser.setUsername(newUsername);
        }
        if (newPassword != null && !newPassword.isBlank()) {
            currentUser.setPassword(passwordEncoder.encode(newPassword));
        }
        return userRepository.save(currentUser);
    }


    String getUserRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .findFirst()
                .orElse("ROLE_NOT_FOUND");
    }
}

