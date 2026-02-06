package com.alonso.salesapp.dto.ingressdetail;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record IngressDetailRequestDTO(

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        Integer idIngressDetail, // ID SIMPLE (Igual que SaleDetail)

        @NotNull(message = "Product ID is required")
        Integer idProduct, // EL PRODUCTO

        @NotNull
        Short quantity,

        @NotNull(message = "Cost is required")
        Double cost // OJO: Aquí es COSTO, no precio de venta
) {
}
