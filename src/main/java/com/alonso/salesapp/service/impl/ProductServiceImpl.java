package com.alonso.salesapp.service.impl;

import com.alonso.salesapp.dto.product.ProductRequestDTO;
import com.alonso.salesapp.dto.product.ProductResponseDTO;
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements IProductService {

    private final ProductRepo repo;
    private final CategoryRepo categoryRepo; // Inyectamos esto para validar
    private final ProductMapper mapper;

    @Transactional
    @Override
    public ProductResponseDTO create(ProductRequestDTO dto) {
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
    public ProductResponseDTO update(Integer id, ProductResponseDTO dto) {
        // 1. Buscar existente
        Product existing = repo.findById(id)
                .orElseThrow(() -> new ModelNotFoundException("Producto no encontrado ID: " + id));

        // 2. Actualizar solo lo que viene
        if (dto.categoryId() != null && !dto.categoryId().equals(existing.getCategory().getIdCategory())) {
            Category category = categoryRepo.findById(dto.categoryId())
                    .orElseThrow(() -> new ModelNotFoundException("Categoría no encontrada ID: " + dto.categoryId()));
            existing.setCategory(category);
        }

        if (dto.name() != null) existing.setName(dto.name());
        if (dto.description() != null) existing.setDescription(dto.description());
        if (dto.price() != null && dto.price() > 0) existing.setPrice(dto.price());
        if (dto.enabled() != null) existing.setEnabled(dto.enabled());

        return mapper.toDTO(repo.save(existing));
    }

    @Override
    public Page<ProductResponseDTO> readAllWithPagination(int page, int size) {
        Pageable pageable = Pageable.ofSize(size).withPage(page);
        return repo.findAll(pageable).map(mapper::toDTO);
    }

    @Override
    public ProductResponseDTO readById(Integer id) {
        return repo.findById(id).map(mapper::toDTO)
                .orElseThrow(() -> new ModelNotFoundException("Producto no encontrado ID: " + id));
    }

    @Transactional
    @Override
    public void delete(Integer id) {
        Product product = repo.findById(id).orElseThrow(() -> new ModelNotFoundException("Producto no encontrado ID: " + id));
        product.setEnabled(false);
        repo.save(product);
    }
}