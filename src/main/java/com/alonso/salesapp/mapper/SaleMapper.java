package com.alonso.salesapp.mapper;

import com.alonso.salesapp.dto.sale.SaleDTO;
import com.alonso.salesapp.dto.sale.SaleResponseDTO;
import com.alonso.salesapp.model.Sale;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {ClientMapper.class, UserMapper.class, SaleDetailMapper.class}
)
public interface SaleMapper {

    // Request: DTO → Entity
    @Mapping(source = "idClient", target = "client.idClient")
    @Mapping(source = "idUser", target = "user.idUser")
    @Mapping(target = "idSale", ignore = true)
    Sale toEntity(SaleDTO dto);

    // Response: Entity → DTO (MapStruct mapea automáticamente)
    @Mapping(source = "total", target = "total")
    SaleResponseDTO toResponseDTO(Sale entity);

    List<SaleResponseDTO> toResponseDTOList(List<Sale> entities);
}