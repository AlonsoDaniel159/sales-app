package com.alonso.salesapp.service.impl;

import com.alonso.salesapp.dto.CategoryDTO;
import com.alonso.salesapp.exception.ModelNotFoundException;
import com.alonso.salesapp.mapper.CategoryMapper;
import com.alonso.salesapp.model.Category;
import com.alonso.salesapp.repository.CategoryRepo;
import com.alonso.salesapp.service.ICategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements ICategoryService {

    private final CategoryRepo repo;
    private final CategoryMapper mapper;

    @Override
    public CategoryDTO create(CategoryDTO dto) {
        Category entity = mapper.toEntity(dto);
        entity = repo.save(entity);
        return mapper.toDTO(entity);
    }

    @Override
    public CategoryDTO update(Integer id, CategoryDTO dto) {
        repo.findById(id).orElseThrow(() -> new ModelNotFoundException("Categoría no encontrada ID: " + id));
        Category entity = mapper.toEntity(dto);
        entity.setIdCategory(id);
        return mapper.toDTO(repo.save(entity));
    }

    @Override
    public List<CategoryDTO> readAll() {
        return repo.findAll()
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    public CategoryDTO readById(Integer id) {
        return repo.findById(id)
                .map(mapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }

    @Override
    public void delete(Integer id) {
        repo.deleteById(id);
    }
}
