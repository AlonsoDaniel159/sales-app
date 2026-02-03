package com.alonso.salesapp.service;

import com.alonso.salesapp.dto.ProductDTO;

import java.util.List;

public interface IProductService {
    ProductDTO create(ProductDTO dto);
    ProductDTO update(Integer id, ProductDTO dto);
    List<ProductDTO> readAll();
    ProductDTO readById(Integer id);
    void delete(Integer id);
}
