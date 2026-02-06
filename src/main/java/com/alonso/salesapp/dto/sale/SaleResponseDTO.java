package com.alonso.salesapp.dto.sale;

import com.alonso.salesapp.dto.client.ClientSummaryDTO;
import com.alonso.salesapp.dto.user.UserSummaryDTO;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

public record SaleResponseDTO(
        Integer idSale,
        ClientSummaryDTO client,
        UserSummaryDTO user,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "America/Lima")
        LocalDateTime dateTime,

        @JsonFormat(shape = JsonFormat.Shape.NUMBER, pattern = "#.00")
        Double total,

        @JsonFormat(shape = JsonFormat.Shape.NUMBER, pattern = "#.00")
        Double tax,

        List<SaleDetailResponseDTO> details
) {}

