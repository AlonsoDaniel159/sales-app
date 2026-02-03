package com.alonso.salesapp.mapper;

import com.alonso.salesapp.dto.ProductDTO;
import com.alonso.salesapp.dto.sale.ProductSummaryDTO;
import com.alonso.salesapp.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductMapper {

    // Al guardar: DTO (id:5) -> Entity (Category{id:5})
    @Mapping(source = "categoryId", target = "category.idCategory")
    Product toEntity(ProductDTO dto);

    // Al leer: Entity (Category{id:5}) -> DTO (id:5)
    @Mapping(source = "category.idCategory", target = "categoryId")
    ProductDTO toDTO(Product entity);

    // Para respuestas de Sale (solo resumen)
    ProductSummaryDTO toSummaryDTO(Product entity);
}
