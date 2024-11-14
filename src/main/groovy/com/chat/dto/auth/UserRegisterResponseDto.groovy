package com.chat.dto.auth

import groovy.transform.Immutable

@Immutable
class UserRegisterResponseDto {
    String id
    String username
    String role
}
