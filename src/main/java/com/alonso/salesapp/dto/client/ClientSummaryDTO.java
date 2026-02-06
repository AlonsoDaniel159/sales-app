package com.alonso.salesapp.dto.client;

public record ClientSummaryDTO(
        Integer idClient,
        String firstName,
        String lastName
) {}