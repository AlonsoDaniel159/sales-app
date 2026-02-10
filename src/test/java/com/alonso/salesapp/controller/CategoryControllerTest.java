package com.alonso.salesapp.controller;

import com.alonso.salesapp.dto.category.CategoryDTO;
import com.alonso.salesapp.exception.ModelNotFoundException;
import com.alonso.salesapp.service.ICategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean  // Mock del servicio
    private ICategoryService service;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {

    }

    @Test
    @DisplayName("Debería retornar todas las categorías cuando se llama a GET /categories")
    void shouldReturnAllCategories_whenGetAllCategoriesIsCalled() throws Exception {
        CategoryDTO category1 = new CategoryDTO(1, "Electronics", "Electronic devices", true);
        CategoryDTO category2 = new CategoryDTO(2, "Books", "Books and magazines", true);
        List<CategoryDTO> categories = List.of(category1, category2);

        when(service.readAll()).thenReturn(categories);

        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].idCategory").value(1))
                .andExpect(jsonPath("$[0].name").value("Electronics"))
                .andExpect(jsonPath("$[0].description").value("Electronic devices"))
                .andExpect(jsonPath("$[0].enabled").value(true));
    }

    @Test
    @DisplayName("Debería crear una categoría cuando se proporcionan datos válidos")
    void shouldCreateCategory_whenValidDataIsProvided() throws Exception {
        CategoryDTO inputDTO = new CategoryDTO(null, "Sports", "Sports equipment", true);
        CategoryDTO createdDTO = new CategoryDTO(1, "Sports", "Sports equipment", true);

        when(service.create(inputDTO)).thenReturn(createdDTO);

        mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.idCategory").value(1))
                .andExpect(jsonPath("$.name").value("Sports"))
                .andExpect(jsonPath("$.description").value("Sports equipment"))
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    @DisplayName("Debería retornar Bad Request cuando el nombre está vacío")
    void shouldReturnBadRequest_whenNameIsEmpty() throws Exception {
        CategoryDTO invalidDTO = new CategoryDTO(null, "", "Description", true);

        mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed with 1 error(s)."));
    }

    @Test
    @DisplayName("Debería retornar Bad Request cuando la descripción no cumple con la validación de tamaño")
    void shouldReturnBadRequest_whenDescriptionIsInvalid() throws Exception {
        CategoryDTO invalidDTO = new CategoryDTO(null, "Sports", "De", true);

        mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.description").value("La descripción debe tener entre 3 y 150 caracteres"));
    }

    @Test
    @DisplayName("Debería actualizar una categoría cuando se proporcionan datos válidos")
    void shouldUpdateCategory_whenValidDataIsProvided() throws Exception {
        CategoryDTO updatedDTO = new CategoryDTO(1, "Electronics Updated", "New description", true);

        when(service.update(eq(1), any(CategoryDTO.class))).thenReturn(updatedDTO);

        mockMvc.perform(put("/categories/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Electronics Updated"));
    }

    @Test
    @DisplayName("Debería eliminar una categoría cuando el ID existe")
    void shouldDeleteCategory_whenIdExists() throws Exception {
        mockMvc.perform(delete("/categories/{id}", 1))
                .andExpect(status().isNoContent());

        verify(service, times(1)).delete(1);
    }

    @Test
    @DisplayName("Debería retornar Not Found cuando la categoría no existe")
    void shouldReturnNotFound_whenCategoryDoesNotExist() throws Exception {
        when(service.readById(999)).thenThrow(new ModelNotFoundException("Category not found"));

        mockMvc.perform(get("/categories/999"))
                .andExpect(status().isNotFound());
    }

}