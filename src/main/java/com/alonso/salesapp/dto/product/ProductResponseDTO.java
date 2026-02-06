package com.alonso.salesapp.dto.product;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

public record ProductResponseDTO(
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        Integer idProduct,

        @NotNull(message = "La categoría es requerida")
        @Min(value = 1, message = "El ID de categoría debe ser válido")
        Integer categoryId, // Solo recibimos el ID (ej: 5)

        @NotNull
        @NotEmpty
        @Size(min = 3, max = 50)
        String name,

        @Size(min = 3, max = 150)
        String description,

        @Min(value = 1, message = "El precio debe ser mayor a 0")
        @JsonFormat(shape = JsonFormat.Shape.NUMBER, pattern = "#.00")
        Double price,

        Integer stock,

        Boolean enabled
) {}