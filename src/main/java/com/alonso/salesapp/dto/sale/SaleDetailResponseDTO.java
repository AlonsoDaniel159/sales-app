package com.alonso.salesapp.dto.sale;

import com.alonso.salesapp.dto.product.ProductSummaryDTO;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.math.RoundingMode;


public record SaleDetailResponseDTO(
        ProductSummaryDTO product,
        Short quantity,

        @JsonFormat(shape = JsonFormat.Shape.NUMBER, pattern = "#.00")
        Double salePrice,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER, pattern = "#.00")
        Double discount
) {
    @JsonFormat(shape = JsonFormat.Shape.NUMBER, pattern = "#.00")
    public Double getSubtotal() {
        if (quantity == null || salePrice == null) {
            return 0.00;
        }
        return BigDecimal.valueOf(quantity)
                .multiply(BigDecimal.valueOf(salePrice))
                .subtract(BigDecimal.valueOf(discount))
                .setScale(2, RoundingMode.HALF_UP) // Aquí defines los 2 decimales matemáticos
                .doubleValue();
    }
}