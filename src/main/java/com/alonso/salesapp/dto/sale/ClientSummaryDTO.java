package com.alonso.salesapp.dto.sale;

public record ClientSummaryDTO(
        Integer idClient,
        String firstName,
        String lastName
) {}