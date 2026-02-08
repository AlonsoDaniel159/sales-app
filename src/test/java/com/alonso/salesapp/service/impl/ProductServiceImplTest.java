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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    private ProductMapper mapper;

    @Mock
    private ICloudinaryService cloudinaryService;

    @InjectMocks
    private ProductServiceImpl productService;

    private Category category;
    private ProductRequestDTO request;
    private Product product;
    private ProductResponseDTO responseDTO;
    private MockMultipartFile validFile;
    private MockMultipartFile emptyFile;
    Integer idProduct;
    String secure_url;
    String public_id;

    @BeforeEach
    void setUp() {
        category = new Category(1, "Electronics", "Electronic devices", true);
        request = new ProductRequestDTO(1, "Laptop", "Gaming laptop", 1500.0, true);
        product = new Product(1, category, "Laptop", "Gaming laptop", 1500.0, 0, null, null, true);
        responseDTO = new ProductResponseDTO(1, 1, "Laptop", "Gaming laptop", 1500.0, 0, null, null, true);
        validFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[]{1, 2, 3});
        emptyFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[]{});
        idProduct = 1;
        secure_url = "https://cloudinary.com/old_image.jpg";
        public_id = "img_123";
    }


    /*
    ========================
    * * * CREATE TESTS * * *
    ========================
    * */
    @Test
    @DisplayName("Create - Debería lanzar exception cuando la categoría no existe")
    void create_ShouldThrowModelNotFoundException_WhenCategoryDoesNotExist() {
        when(categoryRepo.findById(anyInt())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.create(request, validFile))
                .isInstanceOf(ModelNotFoundException.class)
                .hasMessage("Categoría no encontrada ID: " + request.categoryId());

        verify(categoryRepo, times(1)).findById(1);
        verifyNoInteractions(productRepo, mapper, cloudinaryService);
    }

    @Test
    @DisplayName("Create - Debería crear producto sin imagen cuando image es null")
    void create_ShouldNotUploadImage_WhenImageIsNull() {
        when(categoryRepo.findById(1)).thenReturn(Optional.of(category));
        when(mapper.toEntity(request)).thenReturn(product);
        when(productRepo.save(any(Product.class))).thenReturn(product);
        when(mapper.toDTO(product)).thenReturn(responseDTO);

        ProductResponseDTO response = productService.create(request, null);

        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("Laptop");
        assertThat(response.price()).isEqualTo(1500.0);
        assertThat(response.imageUrl()).isNull();

        verify(categoryRepo, times(1)).findById(1);
        verify(productRepo, times(1)).save(any(Product.class));
        verifyNoInteractions(cloudinaryService);
    }

    @Test
    @DisplayName("Create - Debería crear producto sin imagen cuando image está vacía")
    void create_ShouldCreateProductWithoutImage_WhenImageIsEmpty() {
        when(categoryRepo.findById(1)).thenReturn(Optional.of(category));
        when(mapper.toEntity(request)).thenReturn(product);
        when(productRepo.save(any(Product.class))).thenReturn(product);
        when(mapper.toDTO(product)).thenReturn(responseDTO);

        ProductResponseDTO response = productService.create(request, emptyFile);

        assertThat(response).isNotNull();
        assertThat(response.imageUrl()).isNull();

        verifyNoInteractions(cloudinaryService);
    }

    @Test
    @DisplayName("Create - Debería crear producto con imagen exitosamente")
    void create_ShouldCreateProductWithImage_WhenValidFileProvided() {
        Map<String, Object> cloudinaryResult = Map.of(
                "secure_url", secure_url,
                "public_id", public_id
        );
        ProductResponseDTO expectedResponse = new ProductResponseDTO(
                product.getIdProduct(), category.getIdCategory(), "Laptop", "Gaming laptop", 1500.0, 0,
                secure_url, public_id, true
        );

        when(categoryRepo.findById(1)).thenReturn(Optional.of(category));
        when(mapper.toEntity(request)).thenReturn(product);
        when(cloudinaryService.upload(validFile)).thenReturn(cloudinaryResult);
        when(productRepo.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            assertThat(p.getImageUrl()).isEqualTo(secure_url);
            assertThat(p.getImagePublicId()).isEqualTo(public_id);
            return p;
        });
        when(mapper.toDTO(any(Product.class))).thenReturn(expectedResponse);

        ProductResponseDTO response = productService.create(request, validFile);

        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("Laptop");
        assertThat(response.imageUrl()).isEqualTo(secure_url);
        assertThat(response.imagePublicId()).isEqualTo(public_id);

        verify(categoryRepo, times(1)).findById(1);
        verify(cloudinaryService, times(1)).upload(validFile);
        verify(productRepo, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Create - Debería inicializar stock en 0")
    void create_ShouldInitializeStockToZero() {
        when(categoryRepo.findById(1)).thenReturn(Optional.of(category));
        when(mapper.toEntity(request)).thenReturn(product);
        when(productRepo.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            assertThat(p.getStock()).isEqualTo(0);
            return p;
        });
        when(mapper.toDTO(product)).thenReturn(responseDTO);

        productService.create(request, null);

        verify(productRepo).save(argThat(p -> p.getStock() == 0));
    }

    @Test
    @DisplayName("Create - Debería asignar la categoría correctamente")
    void create_ShouldAssignCategory() {
        when(categoryRepo.findById(1)).thenReturn(Optional.of(category));
        when(mapper.toEntity(request)).thenReturn(product);
        when(productRepo.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            assertThat(p.getCategory()).isEqualTo(category);
            return p;
        });
        when(mapper.toDTO(product)).thenReturn(responseDTO);

        productService.create(request, null);

        verify(productRepo).save(argThat(p -> p.getCategory().equals(category)));
    }

    /*
   ========================
   * * * UPDATE TESTS * * *
   ========================
   * */
    @Test
    @DisplayName("Update - Debería lanzar exception cuando el producto no existe")
    void update_ShouldThrowModelNotFoundException_WhenProductDoesNotExist() {
        when(productRepo.findById(anyInt())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.update(1, request, validFile))
                .isInstanceOf(ModelNotFoundException.class)
                .hasMessage("Producto no encontrado ID: " + 1);

        verify(productRepo, times(1)).findById(1);
        verifyNoInteractions(categoryRepo, mapper, cloudinaryService);
    }

    @Test
    @DisplayName("Update - Debería lanzar exception cuando la categoría no existe")
    void update_ShouldThrowModelNotFoundException_WhenCategoryDoesNotExist() {
        ProductRequestDTO requestDto = new ProductRequestDTO(2, "Laptop", "Gaming laptop", 1500.0, true);

        when(productRepo.findById(idProduct)).thenReturn(Optional.of(product));
        when(categoryRepo.findById(anyInt())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.update(idProduct, requestDto, validFile))
                .isInstanceOf(ModelNotFoundException.class)
                .hasMessage("Categoría no encontrada ID: " + requestDto.categoryId());

        verify(productRepo, times(1)).findById(1);
        verify(categoryRepo, times(1)).findById(requestDto.categoryId());
        verifyNoInteractions(mapper, cloudinaryService);
    }

    @Test
    @DisplayName("Update - Debería actualizar los campos correctamente con la imagen vacía")
    void update_ShouldUpdateFieldsCorrectlyWithEmptyImage() {
        ProductRequestDTO requestDTO = new ProductRequestDTO(2, "Laptop", "Gaming laptop", 1500.0, true);

        when(productRepo.findById(idProduct)).thenReturn(Optional.of(product));
        when(categoryRepo.findById(requestDTO.categoryId())).thenReturn(Optional.of(category));
        when(mapper.toDTO(any(Product.class))).thenReturn(responseDTO);
        when(productRepo.save(any(Product.class))).thenReturn(product);

        ProductResponseDTO response = productService.update(idProduct, requestDTO, emptyFile);

        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("Laptop");
        assertThat(response.description()).isEqualTo("Gaming laptop");
        assertThat(response.price()).isEqualTo(1500.0);
        assertThat(response.enabled()).isTrue();

        verify(productRepo, times(1)).findById(idProduct);
        verify(categoryRepo, times(1)).findById(requestDTO.categoryId());
        verify(productRepo, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Update - Debería actualizar los campos correctamente con la imagen nula y con la misma categoría")
    void update_ShouldUpdateFieldsCorrectlyWithNullImageAndSameCategory() {
        when(productRepo.findById(idProduct)).thenReturn(Optional.of(product));
        when(mapper.toDTO(any(Product.class))).thenReturn(responseDTO);
        when(productRepo.save(any(Product.class))).thenReturn(product);

        ProductResponseDTO response = productService.update(idProduct, request, null);

        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("Laptop");
        assertThat(response.description()).isEqualTo("Gaming laptop");
        assertThat(response.price()).isEqualTo(1500.0);
        assertThat(response.enabled()).isTrue();

        verify(productRepo, times(1)).findById(idProduct);
        verify(productRepo, times(1)).save(any(Product.class));
    }


    @Test
    @DisplayName("Update - Debería actualizar producto con imagen exitosamente")
    void update_ShouldCreateProductWithImage_WhenValidFileProvided() {
        Integer idProduct = 1;
        ProductRequestDTO requestDTO = new ProductRequestDTO(null, "Laptop", "Gaming laptop", 1500.0, true);

        Map<String, Object> cloudinaryResult = Map.of(
                "secure_url", "https://cloudinary.com/image.jpg",
                "public_id", "img_123"
        );
        ProductResponseDTO expectedResponse = new ProductResponseDTO(
                product.getIdProduct(), category.getIdCategory(), "Laptop", "Gaming laptop", 1500.0, 0,
                "https://cloudinary.com/image.jpg", "img_123", true
        );

        when(productRepo.findById(idProduct)).thenReturn(Optional.of(product));
        when(cloudinaryService.upload(validFile)).thenReturn(cloudinaryResult);
        when(productRepo.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            assertThat(p.getImageUrl()).isEqualTo("https://cloudinary.com/image.jpg");
            assertThat(p.getImagePublicId()).isEqualTo("img_123");
            return p;
        });
        when(mapper.toDTO(any(Product.class))).thenReturn(expectedResponse);

        ProductResponseDTO response = productService.update(1, requestDTO, validFile);

        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("Laptop");
        assertThat(response.imageUrl()).isEqualTo("https://cloudinary.com/image.jpg");
        assertThat(response.imagePublicId()).isEqualTo("img_123");

        verify(cloudinaryService, times(1)).upload(validFile);
        verify(productRepo, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Update - Debería eliminar la imagen previa cuando se actualiza el producto con una nueva imagen")
    void update_ShouldDeletePreviousImage_WhenNewImageIsProvided() {
        ProductRequestDTO requestDTO = new ProductRequestDTO(null, "Laptop", "Gaming laptop", 1500.0, true);
        product.setImagePublicId("img_123456");

        Map<String, Object> cloudinaryResult = Map.of(
                "secure_url", secure_url,
                "public_id", public_id
        );
        ProductResponseDTO expectedResponse = new ProductResponseDTO(
                product.getIdProduct(), category.getIdCategory(), "Laptop", "Gaming laptop", 1500.0, 0,
                secure_url, public_id, true
        );

        when(productRepo.findById(idProduct)).thenReturn(Optional.of(product));
        when(cloudinaryService.delete(anyString())).thenReturn(null);
        when(cloudinaryService.upload(validFile)).thenReturn(cloudinaryResult);
        when(productRepo.save(any(Product.class))).thenReturn(product);
        when(mapper.toDTO(any(Product.class))).thenReturn(expectedResponse);

        ProductResponseDTO response = productService.update(product.getIdProduct(), requestDTO, validFile);

        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("Laptop");
        assertThat(response.imageUrl()).isEqualTo(secure_url);
        assertThat(response.imagePublicId()).isEqualTo(public_id);

        verify(cloudinaryService, times(1)).upload(validFile);
        verify(cloudinaryService, times(1)).delete("img_123456");
        verify(productRepo, times(1)).save(any(Product.class));
    }

    // ============================================
    // TESTS DE READ
    // ============================================

    @Test
    @DisplayName("ReadById - Debería retornar producto cuando existe")
    void readById_ShouldReturnProduct_WhenExists() {
        // ARRANGE
        when(productRepo.findById(1)).thenReturn(Optional.of(product));
        when(mapper.toDTO(product)).thenReturn(responseDTO);

        // ACT
        ProductResponseDTO response = productService.readById(1);

        // ASSERT
        assertThat(response).isNotNull();
        assertThat(response.idProduct()).isEqualTo(1);
        assertThat(response.name()).isEqualTo("Laptop");

        verify(productRepo, times(1)).findById(1);
    }

    @Test
    @DisplayName("ReadById - Debería lanzar excepción cuando no existe")
    void readById_ShouldThrowException_WhenNotFound() {
        // ARRANGE
        when(productRepo.findById(999)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThatThrownBy(() -> productService.readById(999))
                .isInstanceOf(ModelNotFoundException.class)
                .hasMessageContaining("Producto no encontrado ID: 999");
    }

    @Test
    @DisplayName("ReadAll - Debería retornar página de productos")
    void readAllWithPagination_ShouldReturnPagedProducts() {
        // ARRANGE
        List<Product> products = List.of(product);
        Page<Product> productPage = new PageImpl<>(products, PageRequest.of(0, 10), 1);

        when(productRepo.findAll(any(Pageable.class))).thenReturn(productPage);
        when(mapper.toDTO(product)).thenReturn(responseDTO);

        // ACT
        Page<ProductResponseDTO> response = productService.readAllWithPagination(0, 10);

        // ASSERT
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getContent().getFirst().name()).isEqualTo("Laptop");
    }


    // ============================================
    // TESTS DE DELETE
    // ============================================

    @Test
    @DisplayName("Delete - Debería eliminar producto exitosamente sin imagen asociada")
    void delete_ShouldNotDeleteImage_WhenNoImageAssociated() {
        when(productRepo.findById(1)).thenReturn(Optional.of(product));
        when(productRepo.save(any(Product.class))).thenReturn(product);

        productService.delete(1);

        verify(productRepo, times(1)).findById(1);
        verify(productRepo, times(1)).save(any(Product.class));
        verifyNoInteractions(cloudinaryService);
    }

    @Test
    @DisplayName("Delete - Debería eliminar imagen de Cloudinary si el producto tiene una imagen asociada")
    void delete_ShouldDeleteImage_WhenExists() {
        product.setImagePublicId(public_id);
        when(productRepo.findById(1)).thenReturn(Optional.of(product));
        when(cloudinaryService.delete(public_id)).thenReturn(null);
        when(productRepo.save(any(Product.class))).thenReturn(product);

        productService.delete(1);

        verify(productRepo, times(1)).findById(1);
        verify(cloudinaryService, times(1)).delete(product.getImagePublicId());
        verify(productRepo, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Delete - Debería lanzar excepción cuando producto no existe")
    void delete_ShouldThrowException_WhenNotFound() {
        when(productRepo.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.delete(999))
                .isInstanceOf(ModelNotFoundException.class)
                .hasMessageContaining("Producto no encontrado ID: 999");

        verify(productRepo, times(1)).findById(999);
    }

}
