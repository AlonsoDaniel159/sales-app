package com.alonso.salesapp.mapper;

import com.alonso.salesapp.dto.sale.SaleDetailDTO;
import com.alonso.salesapp.dto.sale.SaleDetailResponseDTO;
import com.alonso.salesapp.model.SaleDetail;
import org.mapstruct.*;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {ProductMapper.class}
)
public interface SaleDetailMapper {

    @Mapping(source = "idProduct", target = "product.idProduct")
    @Mapping(target = "idSaleDetail", ignore = true)
    @Mapping(target = "sale", ignore = true)
    SaleDetail toEntity(SaleDetailDTO dto);

    SaleDetailResponseDTO toResponseDTO(SaleDetail entity);
}