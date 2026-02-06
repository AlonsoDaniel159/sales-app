package com.alonso.salesapp.dto.sale;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SaleDetailDTO(

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        Integer idSaleDetail,

        @NotNull(message = "Product ID is required")
        @Min(value = 1, message = "Product ID must be valid")
        Integer idProduct,

        @NotNull
        @Min(value = 1, message = "Quantity must be at least 1")
        Short quantity,

        Double salePrice,
        Double discount
) {}