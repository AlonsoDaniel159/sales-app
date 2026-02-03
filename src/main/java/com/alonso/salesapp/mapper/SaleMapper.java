package com.alonso.salesapp.mapper;

import com.alonso.salesapp.dto.sale.SaleDTO;
import com.alonso.salesapp.dto.sale.SaleResponseDTO;
import com.alonso.salesapp.model.Sale;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

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
    SaleResponseDTO toResponseDTO(Sale entity);

    List<SaleResponseDTO> toResponseDTOList(List<Sale> entities);
}