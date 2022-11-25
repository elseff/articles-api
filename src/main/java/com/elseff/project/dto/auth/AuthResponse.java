package com.elseff.project.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@AllArgsConstructor
public class AuthResponse {
    private Long id;

    private String email;

    private String token;
}
