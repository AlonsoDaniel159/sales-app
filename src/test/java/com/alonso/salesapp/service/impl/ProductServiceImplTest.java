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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Product Service Tests")
class ProductServiceImplTest {

    @Mock
    private ProductRepo productRepo;

    @Mock
    private CategoryRepo categoryRepo;

    @Mock
    private ProductMapper mapper;

    @Mock
    private ICloudinaryService cloudinaryService;

    @InjectMocks
    private ProductServiceImpl productService;

    private Category createCategory() {
        return new Category(1, "Category 1", "Description", true);
    }

    private ProductRequestDTO createProductRequestDTO() {
        return new ProductRequestDTO(1, "Product 1", "Description 1", 100.0, true);
    }

    private Product createProduct(Category category) {
        return new Product(1, category, "Product 1", "Description 1", 100.0, 0, null, null, true);
    }

    private MockMultipartFile createMockMultipartFile(byte[] content) {
        return new MockMultipartFile("file", "test-image.jpg", "image/jpeg", content);
    }

    @Test
    @DisplayName("Debería lanzar ModelNotFoundException si la categoría no existe")
    void create_ShouldThrowModelNotFoundException_WhenCategoryDoesNotExist() {
        ProductRequestDTO request = createProductRequestDTO();
        MockMultipartFile mockFile = createMockMultipartFile(new byte[1]);

        when(categoryRepo.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(ModelNotFoundException.class, () -> productService.create(request, mockFile));
        verify(categoryRepo).findById(1);
        verifyNoInteractions(productRepo, mapper, cloudinaryService);
    }

    @Test
    @DisplayName("No debería subir la imagen si es null")
    void create_ShouldNotUploadImage_WhenImageIsNull() {
        Category category = createCategory();
        ProductRequestDTO request = createProductRequestDTO();
        Product product = createProduct(category);
        ProductResponseDTO responseDTO = new ProductResponseDTO(1, 1, "Product 1", "Description 1", 100.0, 0, null, null, true);

        when(categoryRepo.findById(1)).thenReturn(Optional.of(category));
        when(mapper.toEntity(request)).thenReturn(product);
        when(productRepo.save(any(Product.class))).thenReturn(product);
        when(mapper.toDTO(product)).thenReturn(responseDTO);

        ProductResponseDTO response = productService.create(request, null);

        assertEquals("Product 1", response.name());
        verify(categoryRepo).findById(1);
        verifyNoInteractions(cloudinaryService);
    }

    @Test
    @DisplayName("No debería subir la imagen si es vacía")
    void create_ShouldNotUploadImage_WhenImageIsEmpty() {
        Category category = createCategory();
        ProductRequestDTO request = createProductRequestDTO();
        Product product = createProduct(category);
        ProductResponseDTO responseDTO = new ProductResponseDTO(1, 1, "Product 1", "Description 1", 100.0, 0, null, null, true);

        when(categoryRepo.findById(1)).thenReturn(Optional.of(category));
        when(mapper.toEntity(request)).thenReturn(product);
        when(productRepo.save(any(Product.class))).thenReturn(product);
        when(mapper.toDTO(product)).thenReturn(responseDTO);

        ProductResponseDTO response = productService.create(request, createMockMultipartFile(new byte[0]));

        assertEquals("Product 1", response.name());
        verify(categoryRepo).findById(1);
        verifyNoInteractions(cloudinaryService);
    }

    @Test
    @DisplayName("Debería crear product exitosamente cuando todos los datos son válidos")
    void create_ShouldCreateProductSuccessfully_WhenAllDataIsValid() {
        Category category = createCategory();
        ProductRequestDTO request = createProductRequestDTO();
        Product product = createProduct(category);
        ProductResponseDTO responseDTO = new ProductResponseDTO(1, 1, "Product 1", "Description 1", 100.0, 0, "http://image.url", "image123", true);
        MockMultipartFile mockFile = createMockMultipartFile(new byte[1]);

        when(categoryRepo.findById(1)).thenReturn(Optional.of(category));
        when(mapper.toEntity(request)).thenReturn(product);
        when(productRepo.save(any(Product.class))).thenReturn(product);
        when(cloudinaryService.upload(mockFile)).thenReturn(Map.of("secure_url", "http://image.url", "public_id", "image123"));
        when(mapper.toDTO(product)).thenReturn(responseDTO);

        ProductResponseDTO response = productService.create(request, mockFile);

        assertEquals("Product 1", response.name());
        verify(categoryRepo).findById(1);
        verify(cloudinaryService).upload(mockFile);
    }
}
