package com.alonso.salesapp.controller;

import com.alonso.salesapp.dto.product.ProductRequestDTO;
import com.alonso.salesapp.dto.product.ProductResponseDTO;
import com.alonso.salesapp.exception.ModelNotFoundException;
import com.alonso.salesapp.service.IProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IProductService productService;

    @Test
    @DisplayName("Debería retornar todos los productos cuando se llama a GET /products")
    void shouldReturnAllProducts_whenGetAllProductsIsCalled() throws Exception {
        ProductResponseDTO product1 = new ProductResponseDTO(1, 1, "High performance laptop", "Description 1",
                2000.00, 15, null, null, true);
        ProductResponseDTO product2 = new ProductResponseDTO(2, 2, "Latest model smartphone", "Description 2",
                1500.00, 20, null, null, true);

        Page<ProductResponseDTO> productsPage = new PageImpl<>(List.of(product1, product2));

        when(productService.readAllWithPagination(0, 10)).thenReturn(productsPage);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[1].idProduct").value(2))
                .andExpect(jsonPath("$.content[0].name").value("High performance laptop"));
    }

    @Test
    @DisplayName("Debería crear un producto cuando se proporcionan datos válidos")
    void shouldCreateProduct_whenValidDataIsProvided() throws Exception {
        ProductRequestDTO inputDTO = new ProductRequestDTO(1, "Product 1", "High performance laptop", 2000.00, true);
        ProductResponseDTO responseDTO = new ProductResponseDTO(1, 1, "Product 1", "High performance laptop",
                2000.00, 0, "https://cloudinary.com/image.jpg", "img_123", true);

        MockMultipartFile file = new MockMultipartFile(
                "image",
                "image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "imagen de prueba" .getBytes()
        );

        MockMultipartFile productPart = new MockMultipartFile(
                "product",
                null,
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsString(inputDTO).getBytes()
        );

        when(productService.create(inputDTO, file)).thenReturn(responseDTO);

        mockMvc.perform(multipart("/api/v1/products")
                        .file(file)
                        .file(productPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.idProduct").value(1))
                .andExpect(jsonPath("$.name").value("Product 1"))
                .andExpect(jsonPath("$.description").value("High performance laptop"))
                .andExpect(jsonPath("$.price").value(2000.00))
                .andExpect(jsonPath("$.imageUrl").value("https://cloudinary.com/image.jpg"))
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    @DisplayName("Debería retornar un producto cuando se proporciona un ID válido")
    void shouldReturnProduct_whenValidIdIsProvided() throws Exception {
        ProductResponseDTO responseDTO = new ProductResponseDTO(1, 1, "Product 1", "High performance laptop",
                2000.00, 0, "https://cloudinary.com/image.jpg", "img_123", true);

        when(productService.readById(1)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/v1/products/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.idProduct").value(1))
                .andExpect(jsonPath("$.name").value("Product 1"))
                .andExpect(jsonPath("$.description").value("High performance laptop"))
                .andExpect(jsonPath("$.price").value(2000.00))
                .andExpect(jsonPath("$.imageUrl").value("https://cloudinary.com/image.jpg"))
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    @DisplayName("Debería actualizar un producto cuando se proporcionan datos válidos")
    void shouldUpdateProduct_whenValidDataIsProvided() throws Exception {
        ProductRequestDTO inputDTO = new ProductRequestDTO(1, "Updated Product", "Updated description", 2500.00, true);
        ProductResponseDTO responseDTO = new ProductResponseDTO(1, 1, "Updated Product", "Updated description",
                2500.00, 0, "https://cloudinary.com/updated_image.jpg", "img_456", true);

        MockMultipartFile file = new MockMultipartFile(
                "image",
                "updated_image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "updated image content".getBytes()
        );

        MockMultipartFile productPart = new MockMultipartFile(
                "product",
                null,
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsString(inputDTO).getBytes()
        );

        when(productService.update(eq(1), any(ProductRequestDTO.class), any(MultipartFile.class)))
                .thenReturn(responseDTO);

        mockMvc.perform(multipart("/api/v1/products/{id}", 1)
                        .file(file)
                        .file(productPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .with(request -> { request.setMethod("PUT"); return request; }))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.idProduct").value(1))
                .andExpect(jsonPath("$.name").value("Updated Product"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.price").value(2500.00))
                .andExpect(jsonPath("$.imageUrl").value("https://cloudinary.com/updated_image.jpg"))
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    @DisplayName("Debería eliminar un producto cuando el ID existe")
    void shouldDeleteProduct_whenIdExists() throws Exception {
        mockMvc.perform(delete("/api/v1/products/{id}", 1))
                .andExpect(status().isNoContent());

        Mockito.verify(productService, Mockito.times(1)).delete(1);
    }

    @Test
    @DisplayName("Debería retornar Not Found cuando el producto no existe")
    void shouldReturnNotFound_whenProductDoesNotExist() throws Exception {
        doThrow(new ModelNotFoundException("Product not found")).when(productService).readById(999);

        mockMvc.perform(get("/api/v1/products/{id}", 999)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    /*

    Test Name: shouldCreateProduct_whenValidDataIsProvided
    @DisplayName: "Debería crear un producto cuando se proporcionan datos válidos"
    Test Name: shouldReturnBadRequest_whenProductNameIsEmpty
    @DisplayName: "Debería retornar Bad Request cuando el nombre del producto está vacío"
    Test Name: shouldReturnBadRequest_whenProductDescriptionIsInvalid
    @DisplayName: "Debería retornar Bad Request cuando la descripción del producto no cumple con la validación de tamaño"
    Test Name: shouldUpdateProduct_whenValidDataIsProvided
    @DisplayName: "Debería actualizar un producto cuando se proporcionan datos válidos"
    Test Name: shouldDeleteProduct_whenIdExists
    @DisplayName: "Debería eliminar un producto cuando el ID existe"
    Test Name: shouldReturnNotFound_whenProductDoesNotExist
    @DisplayName: "Debería retornar Not Found cuando el producto no existe"
     */
}
