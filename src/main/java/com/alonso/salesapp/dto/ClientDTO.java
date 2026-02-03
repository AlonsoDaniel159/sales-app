package com.alonso.salesapp.dto;

import jakarta.validation.constraints.*;

public record ClientDTO(
        Integer idClient,

        @NotNull @NotEmpty @Size(min = 3, max = 100)
        String firstName,

        @NotNull @NotEmpty @Size(min = 3, max = 100)
        String lastName,

        @NotNull @NotEmpty @Size(min = 8, max = 10)
        String cardId,

        @NotNull @NotEmpty @Size(min = 9, max = 10)
        String phoneNumber,

        @NotNull @NotEmpty @Email(message = "Formato de email inválido") // Validación extra
        String email,

        @NotNull @NotEmpty @Size(min = 3, max = 100)
        String address
) {}