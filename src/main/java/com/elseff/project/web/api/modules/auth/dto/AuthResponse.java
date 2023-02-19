package com.elseff.project.web.api.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@ToString
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Authentication response")
public class AuthResponse {

    @Schema(description = "User id")
    Long id;

    @Schema(description = "User email")
    String email;

    @Schema(description = "Basic authentication token")
    String token;
}
