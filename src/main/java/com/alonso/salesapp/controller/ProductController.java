package com.alonso.salesapp.controller;

import com.alonso.salesapp.dto.product.ProductRequestDTO;
import com.alonso.salesapp.dto.product.ProductResponseDTO;
import com.alonso.salesapp.service.IProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/products")
@Tag(name = "Products", description = "Endpoints for managing products")
@RequiredArgsConstructor
public class ProductController {

    private final IProductService service;

    @Operation(summary = "Get all products with pagination", description = "Retrieve a paginated list of products")
    @GetMapping
    public ResponseEntity<Page<ProductResponseDTO>> readAll(@RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "10") int size) {
        Page<ProductResponseDTO> products = service.readAllWithPagination(page, size);
        return ResponseEntity.ok(products);
    }

    @Operation(summary = "Get product by ID", description = "Retrieve a product by its ID")
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> readById(@PathVariable Integer id) {
        return ResponseEntity.ok(service.readById(id));
    }

    @Operation(summary = "Create a new product", description = "Create a new product with the provided details")
    @PostMapping
    public ResponseEntity<ProductResponseDTO> save(@Valid @RequestBody ProductRequestDTO dto) {
        return new ResponseEntity<>(service.create(dto), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing product", description = "Update the details of an existing product by its ID")
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> update(@PathVariable Integer id, @Valid @RequestBody ProductResponseDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @Operation(summary = "Delete a product", description = "Delete a product by its ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}