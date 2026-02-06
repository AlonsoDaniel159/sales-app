package com.alonso.salesapp.service;

import com.alonso.salesapp.dto.product.ProductRequestDTO;
import com.alonso.salesapp.dto.product.ProductResponseDTO;
import org.springframework.data.domain.Page;

public interface IProductService {
    ProductResponseDTO create(ProductRequestDTO dto);
    ProductResponseDTO update(Integer id, ProductResponseDTO dto);
    Page<ProductResponseDTO> readAllWithPagination(int page, int size);
    ProductResponseDTO readById(Integer id);
    void delete(Integer id);
}
