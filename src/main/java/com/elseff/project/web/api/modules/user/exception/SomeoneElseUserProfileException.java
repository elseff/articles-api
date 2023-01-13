package com.elseff.project.web.api.modules.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class SomeoneElseUserProfileException extends RuntimeException {
    public SomeoneElseUserProfileException() {
        super("It's someone else's profile. You can't modify him");
    }
}
