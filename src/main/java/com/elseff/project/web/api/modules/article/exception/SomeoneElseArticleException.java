package com.elseff.project.web.api.modules.article.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class SomeoneElseArticleException extends RuntimeException {
    public SomeoneElseArticleException() {
        super("It's someone else's article. You can't modify her");
    }
}
