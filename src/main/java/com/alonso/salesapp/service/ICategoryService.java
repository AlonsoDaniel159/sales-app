package com.alonso.salesapp.service;

import com.alonso.salesapp.dto.CategoryDTO;
import java.util.List;

public interface ICategoryService {
    CategoryDTO create(CategoryDTO dto);
    CategoryDTO update(Integer id, CategoryDTO dto);
    List<CategoryDTO> readAll();
    CategoryDTO readById(Integer id);
    void delete(Integer id);
}