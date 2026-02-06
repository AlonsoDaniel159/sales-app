package com.alonso.salesapp.mapper;

import com.alonso.salesapp.dto.provider.ProviderDTO;
import com.alonso.salesapp.model.Provider;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProviderMapper {
    Provider toEntity(ProviderDTO dto);
    ProviderDTO toDTO(Provider entity);
}
