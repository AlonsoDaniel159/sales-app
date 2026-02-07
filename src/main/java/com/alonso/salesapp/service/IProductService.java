package com.alonso.salesapp.service;

import com.alonso.salesapp.dto.product.ProductRequestDTO;
import com.alonso.salesapp.dto.product.ProductResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface IProductService {
    ProductResponseDTO create(ProductRequestDTO dto, MultipartFile file);
    ProductResponseDTO update(Integer id, ProductRequestDTO dto, MultipartFile file);
    Page<ProductResponseDTO> readAllWithPagination(int page, int size);
    ProductResponseDTO readById(Integer id);
    void delete(Integer id);
}
