package com.alonso.salesapp.dto.category;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CategoryDTO(
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        Integer idCategory,

        @NotNull(message = "El nombre no puede ser nulo") // Evita null
        @NotEmpty(message = "El nombre no puede estar vacío") // Evita ""
        @Size(min = 3, max = 50, message = "El nombre debe tener entre 3 y 50 caracteres")
        String name,

        @NotNull(message = "La descripción es obligatoria")
        @Size(min = 3, max = 150, message = "La descripción debe tener entre 3 y 150 caracteres")
        String description,

        Boolean enabled
) {}
