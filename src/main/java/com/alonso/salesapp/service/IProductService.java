package com.alonso.salesapp.service;

import com.alonso.salesapp.dto.ProductDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IProductService {
    ProductDTO create(ProductDTO dto);
    ProductDTO update(Integer id, ProductDTO dto);
    Page<ProductDTO> readAllWithPagination(int page, int size);
    ProductDTO readById(Integer id);
    void delete(Integer id);
}
