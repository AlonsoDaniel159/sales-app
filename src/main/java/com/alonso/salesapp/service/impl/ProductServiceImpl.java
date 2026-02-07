package com.alonso.salesapp.service.impl;

import com.alonso.salesapp.dto.product.ProductRequestDTO;
import com.alonso.salesapp.dto.product.ProductResponseDTO;
import com.alonso.salesapp.exception.ModelNotFoundException;
import com.alonso.salesapp.mapper.ProductMapper;
import com.alonso.salesapp.model.Category;
import com.alonso.salesapp.model.Product;
import com.alonso.salesapp.repository.CategoryRepo;
import com.alonso.salesapp.repository.ProductRepo;
import com.alonso.salesapp.service.ICloudinaryService;
import com.alonso.salesapp.service.IProductService;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ProductServiceImpl implements IProductService {

    private final ProductRepo repo;
    private final CategoryRepo categoryRepo; // Inyectamos esto para validar
    private final ProductMapper mapper;
    private final ICloudinaryService cloudinaryService;

    @Transactional
    @Override
    public ProductResponseDTO create(ProductRequestDTO dto, MultipartFile image) {
        // Validamos que la categoría exista. Si no, error 404.
        Category category = categoryRepo.findById(dto.categoryId())
                .orElseThrow(() -> new ModelNotFoundException("Categoría no encontrada ID: " + dto.categoryId()));

        // Convertimos
        Product entity = mapper.toEntity(dto);

        // Asignamos la categoría real encontrada en BD (buena práctica)
        entity.setCategory(category);
        entity.setStock(0); // Inicializamos stock en 0

        if (image != null && !image.isEmpty()) {
            Map result = cloudinaryService.upload(image);
            entity.setImageUrl((String) result.get("secure_url"));
            entity.setImagePublicId((String) result.get("public_id"));
        }

        // Guardamos
        return mapper.toDTO(repo.save(entity));
    }

    @Transactional
    @Override
    public ProductResponseDTO update(Integer id, ProductRequestDTO dto, MultipartFile image) {
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

        if (image != null && !image.isEmpty()) {
            // Eliminar imagen anterior si existe
            if (existing.getImagePublicId() != null) {
                cloudinaryService.delete(existing.getImagePublicId());
            }
            Map result = cloudinaryService.upload(image);
            existing.setImageUrl((String) result.get("secure_url"));
            existing.setImagePublicId((String) result.get("public_id"));
        }

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

        // Eliminar imagen de Cloudinary
        if (product.getImagePublicId() != null) {
            cloudinaryService.delete(product.getImagePublicId());
        }

        repo.save(product);
    }


    //Borrado de imagenes para el create-drop
    @PreDestroy
    public void cleanUpImages() {
        log.info("Eliminando imágenes subidas antes de detener el servicio...");
        repo.findAll().forEach(product -> {
            if (product.getImagePublicId() != null) {
                cloudinaryService.delete(product.getImagePublicId());
            }
        });
    }
}