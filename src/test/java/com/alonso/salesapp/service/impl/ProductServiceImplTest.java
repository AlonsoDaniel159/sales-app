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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("Product Service Tests")
class ProductServiceImplTest {

    @Mock
    private ProductRepo productRepo;

    @Mock
    private CategoryRepo categoryRepo;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ICloudinaryService cloudinaryService;

    @InjectMocks
    private ProductServiceImpl productService;

    private Category category;
    private Product product;
    private ProductRequestDTO requestDTO;
    private ProductResponseDTO responseDTO;
    private MultipartFile mockImage;

    @BeforeEach
    void setUp() {
        // Configurar Category
        category = Category.builder()
                .idCategory(1)
                .name("Electrónicos")
                .description("Productos electrónicos")
                .enabled(true)
                .build();

        // Configurar Product
        product = Product.builder()
                .idProduct(1)
                .category(category)
                .name("Laptop")
                .description("Laptop HP")
                .price(1500.0)
                .stock(0)
                .imageUrl("https://example.com/image.jpg")
                .imagePublicId("public_id_123")
                .enabled(true)
                .build();

        // Configurar DTOs
        requestDTO = new ProductRequestDTO(1, "Laptop", "Laptop HP", 1500.0, true);

        responseDTO = new ProductResponseDTO(1, 1, "Laptop", "Laptop HP", 1500.0, 0,
                "https://example.com/image.jpg", "public_id_123", true);

        mockImage = mock(MultipartFile.class);
    }

    // ============================================
    // TESTS PARA CREATE
    // ============================================
    @Nested
    @DisplayName("Create Product Tests")
    class CreateProductTests {

        @Test
        @DisplayName("Create - Crear producto exitosamente sin imagen")
        void testCreate_Success_WithoutImage() {
            when(categoryRepo.findById(1)).thenReturn(Optional.of(category));
            when(productMapper.toEntity(requestDTO)).thenReturn(product);
            when(productRepo.save(any(Product.class))).thenReturn(product);
            when(productMapper.toDTO(product)).thenReturn(responseDTO);

            ProductResponseDTO result = productService.create(requestDTO, null);

            assertThat(result).isNotNull();
            assertThat(result.idProduct()).isEqualTo(1);
            assertThat(result.name()).isEqualTo("Laptop");
            assertThat(result.stock()).isZero();

            verify(categoryRepo, times(1)).findById(1);
            verify(productMapper, times(1)).toEntity(requestDTO);
            verify(productRepo, times(1)).save(any(Product.class));
            verify(productMapper, times(1)).toDTO(product);
            verify(cloudinaryService, never()).upload(any());
        }

        @Test
        @DisplayName("Create - Crear producto exitosamente con imagen")
        void testCreate_Success_WithImage() {
            Map<String, Object> cloudinaryResult = new HashMap<>();
            cloudinaryResult.put("secure_url", "https://cloudinary.com/image.jpg");
            cloudinaryResult.put("public_id", "cloudinary_public_id");

            when(categoryRepo.findById(1)).thenReturn(Optional.of(category));
            when(productMapper.toEntity(requestDTO)).thenReturn(product);
            when(mockImage.isEmpty()).thenReturn(false);
            when(cloudinaryService.upload(mockImage)).thenReturn(cloudinaryResult);
            when(productRepo.save(any(Product.class))).thenReturn(product);
            when(productMapper.toDTO(product)).thenReturn(responseDTO);

            ProductResponseDTO result = productService.create(requestDTO, mockImage);

            assertThat(result).isNotNull();
            verify(cloudinaryService, times(1)).upload(mockImage);
            verify(productRepo, times(1)).save(any(Product.class));

            // Verificar que se establecieron la URL y el publicId
            ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
            verify(productRepo).save(productCaptor.capture());
            Product savedProduct = productCaptor.getValue();
            assertThat(savedProduct.getCategory()).isEqualTo(category);
            assertThat(savedProduct.getStock()).isZero();
        }

        @Test
        @DisplayName("Create - Crear producto con imagen vacía (no se sube)")
        void testCreate_Success_WithEmptyImage() {
            when(categoryRepo.findById(1)).thenReturn(Optional.of(category));
            when(productMapper.toEntity(requestDTO)).thenReturn(product);
            when(mockImage.isEmpty()).thenReturn(true);
            when(productRepo.save(any(Product.class))).thenReturn(product);
            when(productMapper.toDTO(product)).thenReturn(responseDTO);

            ProductResponseDTO result = productService.create(requestDTO, mockImage);

            assertThat(result).isNotNull();
            verify(cloudinaryService, never()).upload(any());
            verify(productRepo, times(1)).save(any(Product.class));
        }

        @Test
        @DisplayName("Create - Lanza excepción cuando categoría no existe")
        void testCreate_ThrowsException_WhenCategoryNotFound() {
            when(categoryRepo.findById(1)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.create(requestDTO, null))
                    .isInstanceOf(ModelNotFoundException.class)
                    .hasMessageContaining("Categoría no encontrada ID: 1");

            verify(categoryRepo, times(1)).findById(1);
            verify(productMapper, never()).toEntity(any());
            verify(productRepo, never()).save(any());
        }

    }
    // ============================================
    // TESTS PARA UPDATE
    // ============================================

    @Nested
    @DisplayName("Update Product Tests")
    class UpdateProductTests {


        @Test
        @DisplayName("Update - Actualizar producto exitosamente sin cambiar categoría ni imagen")
        void testUpdate_Success_WithoutCategoryAndImageChange() {
            ProductRequestDTO updateDTO = new ProductRequestDTO(1, "Laptop Actualizada", "Nueva descripción", 1800.0, true);
            Product existingProduct = Product.builder()
                    .idProduct(1)
                    .category(category)
                    .name("Laptop")
                    .description("Laptop HP")
                    .price(1500.0)
                    .stock(10)
                    .imageUrl("https://example.com/image.jpg")
                    .imagePublicId("public_id_123")
                    .enabled(true)
                    .build();

            when(productRepo.findById(1)).thenReturn(Optional.of(existingProduct));
            when(productRepo.save(any(Product.class))).thenReturn(existingProduct);
            when(productMapper.toDTO(existingProduct)).thenReturn(responseDTO);

            ProductResponseDTO result = productService.update(1, updateDTO, null);

            assertThat(result).isNotNull();
            verify(productRepo, times(1)).findById(1);
            verify(categoryRepo, never()).findById(anyInt()); // No se busca categoría porque es la misma
            verify(cloudinaryService, never()).upload(any());
            verify(cloudinaryService, never()).delete(any());
            verify(productRepo, times(1)).save(existingProduct);

            assertThat(existingProduct.getName()).isEqualTo("Laptop Actualizada");
            assertThat(existingProduct.getDescription()).isEqualTo("Nueva descripción");
            assertThat(existingProduct.getPrice()).isEqualTo(1800.0);
        }

        @Test
        @DisplayName("Update - Actualizar producto con cambio de categoría")
        void testUpdate_Success_WithCategoryChange() {
            Category newCategory = Category.builder()
                    .idCategory(2)
                    .name("Computadoras")
                    .description("Computadoras y laptops")
                    .enabled(true)
                    .build();

            ProductRequestDTO updateDTO = new ProductRequestDTO(2, "Laptop Actualizada", "Nueva descripción", 1800.0, true);

            Product existingProduct = Product.builder()
                    .idProduct(1)
                    .category(category)
                    .name("Laptop")
                    .description("Laptop HP")
                    .price(1500.0)
                    .stock(10)
                    .enabled(true)
                    .build();

            when(productRepo.findById(1)).thenReturn(Optional.of(existingProduct));
            when(categoryRepo.findById(2)).thenReturn(Optional.of(newCategory));
            when(productRepo.save(any(Product.class))).thenReturn(existingProduct);
            when(productMapper.toDTO(existingProduct)).thenReturn(responseDTO);

            ProductResponseDTO result = productService.update(1, updateDTO, null);

            assertThat(result).isNotNull();
            verify(categoryRepo, times(1)).findById(2);
            verify(productRepo, times(1)).save(existingProduct);
            assertThat(existingProduct.getCategory()).isEqualTo(newCategory);
        }

        @Test
        @DisplayName("Update - Actualizar producto con nueva imagen (elimina la anterior)")
        void testUpdate_Success_WithNewImage() {
            Map<String, Object> cloudinaryResult = new HashMap<>();
            cloudinaryResult.put("secure_url", "https://cloudinary.com/new_image.jpg");
            cloudinaryResult.put("public_id", "new_public_id");

            Product existingProduct = Product.builder()
                    .idProduct(1)
                    .category(category)
                    .name("Laptop")
                    .description("Laptop HP")
                    .price(1500.0)
                    .stock(10)
                    .imageUrl("https://example.com/old_image.jpg")
                    .imagePublicId("old_public_id")
                    .enabled(true)
                    .build();

            when(productRepo.findById(1)).thenReturn(Optional.of(existingProduct));
            when(mockImage.isEmpty()).thenReturn(false);
            when(cloudinaryService.upload(mockImage)).thenReturn(cloudinaryResult);
            when(productRepo.save(any(Product.class))).thenReturn(existingProduct);
            when(productMapper.toDTO(existingProduct)).thenReturn(responseDTO);

            ProductResponseDTO result = productService.update(1, requestDTO, mockImage);

            assertThat(result).isNotNull();
            verify(cloudinaryService, times(1)).delete("old_public_id");
            verify(cloudinaryService, times(1)).upload(mockImage);
            verify(productRepo, times(1)).save(existingProduct);
        }

        @Test
        @DisplayName("Update - Actualizar producto con nueva imagen cuando no había imagen previa")
        void testUpdate_Success_WithNewImageAndNoPreviousImage() {
            Map<String, Object> cloudinaryResult = new HashMap<>();
            cloudinaryResult.put("secure_url", "https://cloudinary.com/new_image.jpg");
            cloudinaryResult.put("public_id", "new_public_id");

            Product existingProduct = Product.builder()
                    .idProduct(1)
                    .category(category)
                    .name("Laptop")
                    .description("Laptop HP")
                    .price(1500.0)
                    .stock(10)
                    .imageUrl(null)
                    .imagePublicId(null)
                    .enabled(true)
                    .build();

            when(productRepo.findById(1)).thenReturn(Optional.of(existingProduct));
            when(mockImage.isEmpty()).thenReturn(false);
            when(cloudinaryService.upload(mockImage)).thenReturn(cloudinaryResult);
            when(productRepo.save(any(Product.class))).thenReturn(existingProduct);
            when(productMapper.toDTO(existingProduct)).thenReturn(responseDTO);

            ProductResponseDTO result = productService.update(1, requestDTO, mockImage);

            assertThat(result).isNotNull();
            verify(cloudinaryService, never()).delete(any()); // No se elimina porque no había imagen previa
            verify(cloudinaryService, times(1)).upload(mockImage);
            verify(productRepo, times(1)).save(existingProduct);
        }

        @Test
        @DisplayName("Update - Actualizar solo campos no nulos")
        void testUpdate_Success_OnlyNonNullFields() {
            ProductRequestDTO updateDTO = new ProductRequestDTO(1, "Laptop Actualizada", null, 1800.0, null);
            Product existingProduct = Product.builder()
                    .idProduct(1)
                    .category(category)
                    .name("Laptop")
                    .description("Descripción original")
                    .price(1500.0)
                    .stock(10)
                    .enabled(true)
                    .build();

            when(productRepo.findById(1)).thenReturn(Optional.of(existingProduct));
            when(productRepo.save(any(Product.class))).thenReturn(existingProduct);
            when(productMapper.toDTO(existingProduct)).thenReturn(responseDTO);

            ProductResponseDTO result = productService.update(1, updateDTO, null);

            assertThat(result).isNotNull();
            assertThat(existingProduct.getName()).isEqualTo("Laptop Actualizada");
            assertThat(existingProduct.getDescription()).isEqualTo("Descripción original"); // No cambió
            assertThat(existingProduct.getPrice()).isEqualTo(1800.0);
            assertThat(existingProduct.isEnabled()).isTrue(); // No cambió
        }

        @Test
        @DisplayName("Update - No actualizar precio si es menor o igual a 0")
        void testUpdate_Success_PriceNotUpdatedWhenZeroOrNegative() {
            ProductRequestDTO updateDTO = new ProductRequestDTO(1, "Laptop", "Descripción", 0.0, true);
            Product existingProduct = Product.builder()
                    .idProduct(1)
                    .category(category)
                    .name("Laptop")
                    .description("Laptop HP")
                    .price(1500.0)
                    .stock(10)
                    .enabled(true)
                    .build();

            when(productRepo.findById(1)).thenReturn(Optional.of(existingProduct));
            when(productRepo.save(any(Product.class))).thenReturn(existingProduct);
            when(productMapper.toDTO(existingProduct)).thenReturn(responseDTO);

            ProductResponseDTO result = productService.update(1, updateDTO, null);

            assertThat(result).isNotNull();
            assertThat(existingProduct.getPrice()).isEqualTo(1500.0); // No cambió
        }

        @Test
        @DisplayName("Update - Actualizar producto cuando categoryId es null (no cambia categoría)")
        void testUpdate_Success_WhenCategoryIdIsNull() {
            ProductRequestDTO updateDTO = new ProductRequestDTO(null, "Laptop Actualizada", "Nueva descripción", 1800.0, true);

            Product existingProduct = Product.builder()
                    .idProduct(1)
                    .category(category)
                    .name("Laptop")
                    .description("Laptop HP")
                    .price(1500.0)
                    .stock(10)
                    .enabled(true)
                    .build();

            when(productRepo.findById(1)).thenReturn(Optional.of(existingProduct));
            when(productRepo.save(any(Product.class))).thenReturn(existingProduct);
            when(productMapper.toDTO(existingProduct)).thenReturn(responseDTO);

            ProductResponseDTO result = productService.update(1, updateDTO, null);

            assertThat(result).isNotNull();
            assertThat(existingProduct.getCategory()).isEqualTo(category); // No cambió
            assertThat(existingProduct.getName()).isEqualTo("Laptop Actualizada");
            assertThat(existingProduct.getDescription()).isEqualTo("Nueva descripción");
            assertThat(existingProduct.getPrice()).isEqualTo(1800.0);
            verify(categoryRepo, never()).findById(anyInt());
        }

        @Test
        @DisplayName("Update - Actualizar con imagen empty (isEmpty retorna true)")
        void testUpdate_Success_WithEmptyImageFile() {
            ProductRequestDTO updateDTO = new ProductRequestDTO(1, "Laptop Actualizada", "Nueva descripción", 1800.0, true);
            Product existingProduct = Product.builder()
                    .idProduct(1)
                    .category(category)
                    .name("Laptop")
                    .description("Laptop HP")
                    .price(1500.0)
                    .stock(10)
                    .imageUrl("https://example.com/old_image.jpg")
                    .imagePublicId("old_public_id")
                    .enabled(true)
                    .build();

            when(productRepo.findById(1)).thenReturn(Optional.of(existingProduct));
            when(mockImage.isEmpty()).thenReturn(true); // Imagen vacía
            when(productRepo.save(any(Product.class))).thenReturn(existingProduct);
            when(productMapper.toDTO(existingProduct)).thenReturn(responseDTO);

            ProductResponseDTO result = productService.update(1, updateDTO, mockImage);

            assertThat(result).isNotNull();
            verify(cloudinaryService, never()).delete(any()); // No se elimina
            verify(cloudinaryService, never()).upload(any()); // No se sube
            verify(productRepo, times(1)).save(existingProduct);
        }

        @Test
        @DisplayName("Update - Lanza excepción cuando producto no existe")
        void testUpdate_ThrowsException_WhenProductNotFound() {
            when(productRepo.findById(1)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.update(1, requestDTO, null))
                    .isInstanceOf(ModelNotFoundException.class)
                    .hasMessageContaining("Producto no encontrado ID: 1");

            verify(productRepo, times(1)).findById(1);
            verify(productRepo, never()).save(any());
        }

        @Test
        @DisplayName("Update - Lanza excepción cuando categoría nueva no existe")
        void testUpdate_ThrowsException_WhenNewCategoryNotFound() {
            ProductRequestDTO updateDTO = new ProductRequestDTO(
                    99, // Categoría inexistente
                    "Laptop",
                    "Descripción",
                    1500.0,
                    true
            );

            Product existingProduct = Product.builder()
                    .idProduct(1)
                    .category(category)
                    .name("Laptop")
                    .description("Laptop HP")
                    .price(1500.0)
                    .stock(10)
                    .enabled(true)
                    .build();

            when(productRepo.findById(1)).thenReturn(Optional.of(existingProduct));
            when(categoryRepo.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.update(1, updateDTO, null))
                    .isInstanceOf(ModelNotFoundException.class)
                    .hasMessageContaining("Categoría no encontrada ID: 99");

            verify(categoryRepo, times(1)).findById(99);
            verify(productRepo, never()).save(any());
        }
    }
    // ============================================
    // TESTS TESTS
    // ============================================

    @Nested
    @DisplayName("Read Product Tests")
    class ReadProductTests {
        @Test
        @DisplayName("ReadAllWithPagination - Obtener página de productos exitosamente")
        void testReadAllWithPagination_Success() {
            List<Product> products = List.of(product);
            Page<Product> productPage = new PageImpl<>(products);

            when(productRepo.findAll(any(Pageable.class))).thenReturn(productPage);
            when(productMapper.toDTO(any(Product.class))).thenReturn(responseDTO);

            Page<ProductResponseDTO> result = productService.readAllWithPagination(0, 10);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().getFirst().name()).isEqualTo("Laptop");

            verify(productRepo, times(1)).findAll(any(Pageable.class));
            verify(productMapper, times(1)).toDTO(product);
        }

        @Test
        @DisplayName("ReadAllWithPagination - Obtener página vacía")
        void testReadAllWithPagination_EmptyPage() {
            Page<Product> emptyPage = new PageImpl<>(List.of());

            when(productRepo.findAll(any(Pageable.class))).thenReturn(emptyPage);

            Page<ProductResponseDTO> result = productService.readAllWithPagination(0, 10);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();

            verify(productRepo, times(1)).findAll(any(Pageable.class));
            verify(productMapper, never()).toDTO(any());
        }

        // ============================================
        // TESTS PARA READ BY ID
        // ============================================

        @Test
        @DisplayName("ReadById - Obtener producto por ID exitosamente")
        void testReadById_Success() {
            when(productRepo.findById(1)).thenReturn(Optional.of(product));
            when(productMapper.toDTO(product)).thenReturn(responseDTO);

            ProductResponseDTO result = productService.readById(1);

            assertThat(result).isNotNull();
            assertThat(result.idProduct()).isEqualTo(1);
            assertThat(result.name()).isEqualTo("Laptop");

            verify(productRepo, times(1)).findById(1);
            verify(productMapper, times(1)).toDTO(product);
        }

        @Test
        @DisplayName("ReadById - Lanza excepción cuando producto no existe")
        void testReadById_ThrowsException_WhenProductNotFound() {
            when(productRepo.findById(1)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.readById(1))
                    .isInstanceOf(ModelNotFoundException.class)
                    .hasMessageContaining("Producto no encontrado ID: 1");

            verify(productRepo, times(1)).findById(1);
            verify(productMapper, never()).toDTO(any());
        }
    }
    // ============================================
    // TESTS PARA DELETE
    // ============================================

    @Nested
    @DisplayName("Delete Product Tests")
    class DeleteProductTests {
        @Test
        @DisplayName("Delete - Deshabilitar producto exitosamente con imagen")
        void testDelete_Success_WithImage() {
            when(productRepo.findById(1)).thenReturn(Optional.of(product));
            when(productRepo.save(any(Product.class))).thenReturn(product);

            productService.delete(1);

            verify(productRepo, times(1)).findById(1);
            verify(cloudinaryService, times(1)).delete("public_id_123");
            verify(productRepo, times(1)).save(product);
            assertThat(product.isEnabled()).isFalse();
        }

        @Test
        @DisplayName("Delete - Deshabilitar producto sin imagen")
        void testDelete_Success_WithoutImage() {
            Product productWithoutImage = Product.builder()
                    .idProduct(1)
                    .category(category)
                    .name("Laptop")
                    .description("Laptop HP")
                    .price(1500.0)
                    .stock(10)
                    .imageUrl(null)
                    .imagePublicId(null)
                    .enabled(true)
                    .build();

            when(productRepo.findById(1)).thenReturn(Optional.of(productWithoutImage));
            when(productRepo.save(any(Product.class))).thenReturn(productWithoutImage);

            productService.delete(1);

            verify(productRepo, times(1)).findById(1);
            verify(cloudinaryService, never()).delete(any());
            verify(productRepo, times(1)).save(productWithoutImage);
            assertThat(productWithoutImage.isEnabled()).isFalse();
        }

        @Test
        @DisplayName("Delete - Lanza excepción cuando producto no existe")
        void testDelete_ThrowsException_WhenProductNotFound() {
            when(productRepo.findById(1)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.delete(1))
                    .isInstanceOf(ModelNotFoundException.class)
                    .hasMessageContaining("Producto no encontrado ID: 1");

            verify(productRepo, times(1)).findById(1);
            verify(cloudinaryService, never()).delete(any());
            verify(productRepo, never()).save(any());
        }
    }
}