
package com.alonso.salesapp.dto.sale;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SaleDTO(
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        Integer idSale,

        @NotNull(message = "Client ID is required")
        @Min(value = 1, message = "Client ID must be valid")
        Integer idClient,

        @NotNull(message = "User ID is required")
        @Min(value = 1, message = "User ID must be valid")
        Integer idUser,

        LocalDateTime dateTime,
        Double total,
        Double tax,

        @NotNull
        @NotEmpty(message = "La venta debe tener al menos un producto") // Que no esté vacía []
        List<SaleDetailDTO> details
) {}