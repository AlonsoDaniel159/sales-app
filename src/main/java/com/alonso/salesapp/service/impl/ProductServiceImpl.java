package com.alonso.salesapp.service.impl;

import com.alonso.salesapp.dto.ProductDTO;
import com.alonso.salesapp.exception.ModelNotFoundException;
import com.alonso.salesapp.mapper.ProductMapper;
import com.alonso.salesapp.model.Category;
import com.alonso.salesapp.model.Product;
import com.alonso.salesapp.repository.CategoryRepo;
import com.alonso.salesapp.repository.ProductRepo;
import com.alonso.salesapp.service.IProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements IProductService {

    private final ProductRepo repo;
    private final CategoryRepo categoryRepo; // Inyectamos esto para validar
    private final ProductMapper mapper;

    @Transactional
    @Override
    public ProductDTO create(ProductDTO dto) {
        // 1. Validamos que la categoría exista. Si no, error 404.
        Category category = categoryRepo.findById(dto.categoryId())
                .orElseThrow(() -> new ModelNotFoundException("Categoría no encontrada ID: " + dto.categoryId()));

        // 2. Convertimos
        Product entity = mapper.toEntity(dto);

        // 3. Asignamos la categoría real encontrada en BD (buena práctica)
        entity.setCategory(category);
        entity.setStock(0); // Inicializamos stock en 0

        // 4. Guardamos
        return mapper.toDTO(repo.save(entity));
    }

    @Transactional
    @Override
    public ProductDTO update(Integer id, ProductDTO dto) {
        // Validar producto
        repo.findById(id).orElseThrow(() -> new ModelNotFoundException("Producto no encontrado ID: " + id));

        // Validar categoría nueva
        Category category = categoryRepo.findById(dto.categoryId())
                .orElseThrow(() -> new ModelNotFoundException("Categoría no encontrada ID: " + dto.categoryId()));

        Product entity = mapper.toEntity(dto);
        entity.setIdProduct(id);
        entity.setCategory(category);

        return mapper.toDTO(repo.save(entity));
    }

    @Override
    public Page<ProductDTO> readAllWithPagination(int page, int size) {
        Pageable pageable = Pageable.ofSize(size).withPage(page);
        return repo.findAll(pageable).map(mapper::toDTO);
    }

    @Override
    public ProductDTO readById(Integer id) {
        return repo.findById(id).map(mapper::toDTO)
                .orElseThrow(() -> new ModelNotFoundException("Producto no encontrado ID: " + id));
    }

    @Override
    public void delete(Integer id) {
        repo.findById(id).orElseThrow(() -> new ModelNotFoundException("Producto no encontrado ID: " + id));
        repo.deleteById(id);
    }
}