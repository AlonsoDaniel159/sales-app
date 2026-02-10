package com.alonso.salesapp.dto.role;
import jakarta.validation.constraints.*;

public record RoleDTO(
        @NotNull
        Integer idRole,

        @NotNull
        @NotEmpty
        String name,

        Boolean enabled
) {}