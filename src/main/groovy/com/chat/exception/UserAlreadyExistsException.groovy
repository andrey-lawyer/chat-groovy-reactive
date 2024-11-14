package com.chat.exception;
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class UserAlreadyExistsException extends ResponseStatusException {
    UserAlreadyExistsException() {
        super(HttpStatus.CONFLICT , "Username already exists" );
    }
}
