package com.alonso.salesapp.dto.ingress;

import com.alonso.salesapp.dto.sale.ProductSummaryDTO;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record IngressDetailResponseDTO(

        Integer idIngressDetail, // ID SIMPLE (Igual que SaleDetail)
        ProductSummaryDTO product, // EL PRODUCTO
        Short quantity,

        @JsonFormat(shape = JsonFormat.Shape.NUMBER, pattern = "#.00")
        Double cost
) {
    @JsonFormat(shape = JsonFormat.Shape.NUMBER, pattern = "#.00")
    public Double getSubtotal() {
        if (quantity == null || cost == null) {
            return 0.00;
        }
        return BigDecimal.valueOf(quantity)
                .multiply(BigDecimal.valueOf(cost))
                .setScale(2, RoundingMode.HALF_UP) // Aquí defines los 2 decimales matemáticos
                .doubleValue();
    }
}
