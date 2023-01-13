package com.elseff.project.web.api.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@AllArgsConstructor
@Schema(description = "Authentication response")
public class AuthResponse {

    @Schema(description = "User id")
    private Long id;

    @Schema(description = "User email")
    private String email;

    @Schema(description = "Basic authentication token")
    private String token;
}
