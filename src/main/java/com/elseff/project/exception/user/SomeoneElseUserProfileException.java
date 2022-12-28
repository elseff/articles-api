package com.elseff.project.exception.user;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class SomeoneElseUserProfileException extends RuntimeException {
    public SomeoneElseUserProfileException() {
        super("It's someone else's profile. You can't modify him");
    }
}
