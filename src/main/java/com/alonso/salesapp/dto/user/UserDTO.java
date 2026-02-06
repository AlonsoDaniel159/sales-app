package com.alonso.salesapp.dto.user;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonProperty;

public record UserDTO(
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        Integer idUser,
        @NotNull Integer idRole, // Relación plana
        @NotNull @NotEmpty String username,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // El password solo se escribe, no se devuelve
        @NotNull @NotEmpty String password,
        Boolean enabled
) {}