package com.elseff.project.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class IdLessThanZeroException extends RuntimeException{
    public IdLessThanZeroException(){
        super("id must be a greater than 0");
    }
}
