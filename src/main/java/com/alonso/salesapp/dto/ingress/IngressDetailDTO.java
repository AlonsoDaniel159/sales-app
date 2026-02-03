package com.alonso.salesapp.dto.ingress;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record IngressDetailDTO(

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        Integer idIngressDetail, // ID SIMPLE (Igual que SaleDetail)

        @NotNull(message = "Product ID is required")
        Integer idProduct, // EL PRODUCTO

        @NotNull
        @Min(value = 1, message = "Quantity must be at least 1")
        Short quantity,

        @NotNull(message = "Cost is required")
        @Min(value = 0, message = "Cost cannot be negative")
        Double cost // OJO: Aquí es COSTO, no precio de venta
) {
}
