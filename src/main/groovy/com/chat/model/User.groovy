package com.chat.model

import com.fasterxml.jackson.annotation.JsonIgnore
import lombok.Data
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
//import org.springframework.security.core.GrantedAuthority
//import org.springframework.security.core.authority.SimpleGrantedAuthority

import java.time.Instant

@Document(collection = "users")
@Data
class User {

    @Id
    String id

    @Indexed(unique = true)
    String username

    @JsonIgnore
    String password

    String role

    Instant registeredAt = Instant.now()

//    Collection<? extends GrantedAuthority> getAuthorities() {
//        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
//    }
}



