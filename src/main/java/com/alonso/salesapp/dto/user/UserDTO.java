package com.alonso.salesapp.dto.user;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonProperty;

public record UserDTO(
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        Integer idUser,
        @NotNull Integer idRole, // Relación plana
        @NotNull @NotEmpty String username,
        @NotNull @NotEmpty String password,
        Boolean enabled
) {}