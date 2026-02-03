package com.alonso.salesapp.mapper;

import com.alonso.salesapp.dto.ClientDTO;
import com.alonso.salesapp.dto.sale.ClientSummaryDTO;
import com.alonso.salesapp.model.Client;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ClientMapper {

    Client toEntity(ClientDTO dto);

    ClientDTO toDTO(Client entity);

    // Para respuestas de Sale (solo resumen)
    ClientSummaryDTO toSummaryDTO(Client entity);
}