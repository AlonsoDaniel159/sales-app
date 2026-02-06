package com.alonso.salesapp.dto.ingress;

import com.alonso.salesapp.dto.ingressdetail.IngressDetailResponseDTO;
import com.alonso.salesapp.dto.provider.ProviderSummaryDTO;
import com.alonso.salesapp.dto.user.UserSummaryDTO;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

public record IngressResponseDTO(
        Integer idIngress,
        ProviderSummaryDTO provider,
        UserSummaryDTO user,
        String serialNumber,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "America/Lima")
        LocalDateTime dateTime,

        @JsonFormat(shape = JsonFormat.Shape.NUMBER, pattern = "#.00")
        Double total,

        @JsonFormat(shape = JsonFormat.Shape.NUMBER, pattern = "#.00")
        Double tax,

        List<IngressDetailResponseDTO> details
) {
}
