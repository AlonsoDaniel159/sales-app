package com.alonso.salesapp.dto.product;

public record ProductSummaryDTO(
        Integer idProduct,
        String name,
        Double price
) {}