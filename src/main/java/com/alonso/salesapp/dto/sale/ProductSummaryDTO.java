package com.alonso.salesapp.dto.sale;

public record ProductSummaryDTO(
        Integer idProduct,
        String name,
        Double price
) {}