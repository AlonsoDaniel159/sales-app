package com.alonso.salesapp.dto;
import jakarta.validation.constraints.*;

public record RoleDTO(
        Integer idRole,
        @NotNull @NotEmpty String name,
        Boolean enabled
) {}