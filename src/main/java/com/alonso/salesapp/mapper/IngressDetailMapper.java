package com.alonso.salesapp.mapper;

import com.alonso.salesapp.dto.ingressdetail.IngressDetailRequestDTO;
import com.alonso.salesapp.dto.ingressdetail.IngressDetailResponseDTO;
import com.alonso.salesapp.model.IngressDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {ProductMapper.class})
public interface IngressDetailMapper {

    @Mapping(source = "idProduct", target = "product.idProduct")
    @Mapping(target = "idIngressDetail", ignore = true)
    @Mapping(target = "ingress", ignore = true)
    IngressDetail toEntity(IngressDetailRequestDTO dto);

    IngressDetailResponseDTO toResponseDTO(IngressDetail entity);
}
