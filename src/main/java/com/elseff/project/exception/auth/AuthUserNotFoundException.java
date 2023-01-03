package com.elseff.project.exception.auth;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class AuthUserNotFoundException extends RuntimeException{
    public AuthUserNotFoundException(String message) {
        super(message);
    }
}
