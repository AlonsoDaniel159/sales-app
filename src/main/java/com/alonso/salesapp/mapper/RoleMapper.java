package com.alonso.salesapp.mapper;

import com.alonso.salesapp.dto.RoleDTO;
import com.alonso.salesapp.model.Role;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RoleMapper {
    Role toEntity(RoleDTO dto);
    RoleDTO toDTO(Role entity);
}
