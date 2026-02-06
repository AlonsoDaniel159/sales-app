package com.alonso.salesapp.mapper;

import com.alonso.salesapp.dto.user.UserDTO;
import com.alonso.salesapp.dto.user.UserSummaryDTO;
import com.alonso.salesapp.model.User;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    @Mapping(source = "idRole", target = "role.idRole")
    User toEntity(UserDTO dto);

    @Mapping(source = "role.idRole", target = "idRole")
    UserDTO toDTO(User entity);

    // Para respuestas de Sale (solo resumen)
    UserSummaryDTO toSummaryDTO(User entity);
}