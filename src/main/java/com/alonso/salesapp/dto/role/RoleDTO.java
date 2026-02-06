package com.alonso.salesapp.dto.role;
import jakarta.validation.constraints.*;

public record RoleDTO(
        @NotNull @NotEmpty Integer idRole,
        @NotNull @NotEmpty String name,
        Boolean enabled
) {}