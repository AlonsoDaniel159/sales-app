package com.alonso.salesapp.repository;

import com.alonso.salesapp.model.Category;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Slf4j
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepo categoryRepo;

    @Test
    void findAllCategories_whenCategoriesExist_shouldReturnList() {
        // Given - Preparar datos de prueba
        Category category1 = new Category();
        category1.setName("Electronics");
        category1.setDescription("Electronic devices");
        category1.setEnabled(true);

        Category category2 = new Category();
        category2.setName("Books");
        category2.setDescription("All kinds of books");
        category2.setEnabled(true);

        categoryRepo.save(category1);
        categoryRepo.save(category2);

        // When - Ejecutar el método a probar
        List<Category> categories = categoryRepo.findAll();

        // Then - Verificar resultados
        log.info("Categories: {}", categories);
        assertNotNull(categories);
        assertEquals(2, categories.size());
    }

    @Test
    void findAllCategories_whenNoCategories_shouldReturnEmptyList() {
        // When
        List<Category> categories = categoryRepo.findAll();

        // Then
        assertNotNull(categories);
        assertTrue(categories.isEmpty());
    }

    @Test
    void saveCategory_shouldGenerateId() {
        // Given
        Category category = new Category();
        category.setName("Sports");
        category.setDescription("Sports equipment");
        category.setEnabled(true);

        // When
        Category savedCategory = categoryRepo.save(category);

        // Then
        assertNotNull(savedCategory.getIdCategory());
        assertEquals("Sports", savedCategory.getName());
    }
}