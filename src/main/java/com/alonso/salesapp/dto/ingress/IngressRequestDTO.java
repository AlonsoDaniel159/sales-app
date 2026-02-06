package com.alonso.salesapp.dto.ingress;

import com.alonso.salesapp.dto.ingressdetail.IngressDetailRequestDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record IngressRequestDTO(
        Integer idIngress,

        @NotNull(message = "Provider ID is required")
        @Min(value = 1, message = "Provider ID must be valid")
        Integer idProvider, // El proveedor

        @NotNull(message = "User ID is required")
        @Min(value = 1, message = "User ID must be valid")
        Integer idUser,

        @NotNull
        @NotNull(message = "Serial number is required")
        String serialNumber, // Nro de factura física

        LocalDateTime dateTime,
        Double tax,

        @NotNull
        @NotEmpty(message = "El ingreso debe tener al menos un producto") // Que no esté vacía []
        @Valid
        List<IngressDetailRequestDTO> details
) {}