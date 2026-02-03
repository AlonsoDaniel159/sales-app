package com.alonso.salesapp.mapper;

import com.alonso.salesapp.dto.CategoryDTO;
import com.alonso.salesapp.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CategoryMapper {
    CategoryDTO toDTO(Category entity);
    Category toEntity(CategoryDTO dto);
}
