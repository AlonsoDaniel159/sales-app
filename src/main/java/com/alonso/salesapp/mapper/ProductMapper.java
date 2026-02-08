package com.alonso.salesapp.mapper;

import com.alonso.salesapp.dto.product.ProductRequestDTO;
import com.alonso.salesapp.dto.product.ProductResponseDTO;
import com.alonso.salesapp.dto.product.ProductSummaryDTO;
import com.alonso.salesapp.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductMapper {

    // Al guardar: DTO (id:5) -> Entity (Category{id:5})
    @Mapping(source = "categoryId", target = "category.idCategory")
    @Mapping(target = "idProduct", ignore = true)
    Product toEntity(ProductRequestDTO dto);

    // Al leer: Entity (Category{id:5}) -> DTO (id:5)
    @Mapping(source = "category.idCategory", target = "categoryId")
    ProductResponseDTO toDTO(Product entity);

    // Para respuestas de Sale (solo resumen)
    ProductSummaryDTO toSummaryDTO(Product entity);
}
