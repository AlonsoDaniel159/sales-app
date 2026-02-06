package com.alonso.salesapp.mapper;

import com.alonso.salesapp.dto.ingress.IngressRequestDTO;
import com.alonso.salesapp.dto.ingress.IngressResponseDTO;
import com.alonso.salesapp.model.Ingress;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {ProviderMapper.class, UserMapper.class, IngressDetailMapper.class})
public interface IngressMapper {

    @Mapping(source = "idProvider", target = "provider.idProvider")
    @Mapping(source = "idUser", target = "user.idUser")
    @Mapping(target = "idIngress", ignore = true)
    Ingress toEntity(IngressRequestDTO dto);

    IngressResponseDTO toResponseDTO(Ingress entity);

    List<IngressResponseDTO> toResponseDTOList(List<Ingress> entities);
}
