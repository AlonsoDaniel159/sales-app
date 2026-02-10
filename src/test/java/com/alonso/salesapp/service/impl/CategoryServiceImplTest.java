package com.alonso.salesapp.service.impl;

import com.alonso.salesapp.dto.category.CategoryDTO;
import com.alonso.salesapp.exception.ModelNotFoundException;
import com.alonso.salesapp.mapper.CategoryMapper;
import com.alonso.salesapp.model.Category;
import com.alonso.salesapp.repository.CategoryRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Category Service Tests")
class CategoryServiceImplTest {

    @Mock
    private CategoryRepo repo;

    @Mock
    private CategoryMapper mapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category;
    private CategoryDTO categoryDTO;

    @BeforeEach
    void setUp() {
        category = new Category(1, "Electronics", "Electronic devices", true);
        categoryDTO = new CategoryDTO(1, "Electronics", "Electronic devices", true);
    }

    @Nested
    @DisplayName("Crear Categoría")
    class CreateTests {

        @Test
        @DisplayName("Debería crear categoría exitosamente")
        void shouldCreateCategory_Successfully() {
            when(mapper.toEntity(categoryDTO)).thenReturn(category);
            when(repo.save(any(Category.class))).thenReturn(category);
            when(mapper.toDTO(category)).thenReturn(categoryDTO);

            CategoryDTO result = categoryService.create(categoryDTO);

            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("Electronics");
            verify(mapper).toEntity(categoryDTO);
            verify(repo).save(category);
            verify(mapper).toDTO(category);
        }

        @Test
        @DisplayName("Debería llamar al repositorio con la entidad correcta")
        void shouldCallRepository_WithCorrectEntity() {
            when(mapper.toEntity(categoryDTO)).thenReturn(category);
            when(repo.save(any(Category.class))).thenReturn(category);
            when(mapper.toDTO(category)).thenReturn(categoryDTO);

            categoryService.create(categoryDTO);

            ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
            verify(repo).save(captor.capture());
            Category savedCategory = captor.getValue();
            assertThat(savedCategory.getName()).isEqualTo("Electronics");
        }
    }

    @Nested
    @DisplayName("Actualizar Categoría")
    class UpdateTests {

        @Test
        @DisplayName("Debería actualizar categoría exitosamente")
        void shouldUpdateCategory_Successfully() {
            CategoryDTO updateDTO = new CategoryDTO(null, "Updated", "Updated description", true);
            Category updatedCategory = new Category(1, "Updated", "Updated description", true);
            CategoryDTO responseDTO = new CategoryDTO(1, "Updated", "Updated description", true);

            when(repo.findById(1)).thenReturn(Optional.of(category));
            when(mapper.toEntity(updateDTO)).thenReturn(updatedCategory);
            when(repo.save(any(Category.class))).thenReturn(updatedCategory);
            when(mapper.toDTO(updatedCategory)).thenReturn(responseDTO);

            CategoryDTO result = categoryService.update(1, updateDTO);

            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("Updated");
            verify(repo).findById(1);
            verify(repo).save(any(Category.class));
        }

        @Test
        @DisplayName("Debería lanzar exception cuando categoría no existe")
        void shouldThrowException_WhenCategoryNotFound() {
            CategoryDTO updateDTO = new CategoryDTO(null, "Updated", "Updated description", true);

            when(repo.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.update(999, updateDTO))
                    .isInstanceOf(ModelNotFoundException.class)
                    .hasMessageContaining("Categoría no encontrada ID: 999");

            verify(repo).findById(999);
            verifyNoMoreInteractions(mapper, repo);
        }

        @Test
        @DisplayName("Debería asignar el ID correcto a la entidad")
        void shouldAssignCorrectId_ToEntity() {
            CategoryDTO updateDTO = new CategoryDTO(null, "Updated", "Updated description", true);

            when(repo.findById(1)).thenReturn(Optional.of(category));
            when(mapper.toEntity(updateDTO)).thenReturn(category);
            when(repo.save(any(Category.class))).thenReturn(category);
            when(mapper.toDTO(category)).thenReturn(categoryDTO);

            categoryService.update(1, updateDTO);

            ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
            verify(repo).save(captor.capture());
            assertThat(captor.getValue().getIdCategory()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Consultar Categorías")
    class ReadTests {

        @Test
        @DisplayName("Debería retornar todas las categorías")
        void shouldReturnAllCategories() {
            Category category2 = new Category(2, "Books", "Book category", true);
            CategoryDTO categoryDTO2 = new CategoryDTO(2, "Books", "Book category", true);

            when(repo.findAll()).thenReturn(List.of(category, category2));
            when(mapper.toDTO(category)).thenReturn(categoryDTO);
            when(mapper.toDTO(category2)).thenReturn(categoryDTO2);

            List<CategoryDTO> result = categoryService.readAll();

            assertThat(result).hasSize(2);
            assertThat(result).extracting(CategoryDTO::name)
                    .containsExactly("Electronics", "Books");
            verify(repo).findAll();
        }

        @Test
        @DisplayName("Debería retornar lista vacía cuando no hay categorías")
        void shouldReturnEmptyList_WhenNoCategoriesExist() {
            when(repo.findAll()).thenReturn(List.of());

            List<CategoryDTO> result = categoryService.readAll();

            assertThat(result).isEmpty();
            verify(repo).findAll();
        }

        @Test
        @DisplayName("Debería retornar categoría por ID")
        void shouldReturnCategory_ById() {
            when(repo.findById(1)).thenReturn(Optional.of(category));
            when(mapper.toDTO(category)).thenReturn(categoryDTO);

            CategoryDTO result = categoryService.readById(1);

            assertThat(result).isNotNull();
            assertThat(result.idCategory()).isEqualTo(1);
            assertThat(result.name()).isEqualTo("Electronics");
            verify(repo).findById(1);
        }

        @Test
        @DisplayName("Debería lanzar exception cuando categoría no existe")
        void shouldThrowException_WhenCategoryNotFoundById() {
            when(repo.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.readById(999))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Category not found");

            verify(repo).findById(999);
        }
    }

    @Nested
    @DisplayName("Eliminar Categoría")
    class DeleteTests {

        @Test
        @DisplayName("Debería realizar eliminación lógica")
        void shouldPerformLogicalDelete() {
            when(repo.findById(1)).thenReturn(Optional.of(category));
            when(repo.save(any(Category.class))).thenReturn(category);

            categoryService.delete(1);

            ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
            verify(repo).save(captor.capture());
        }

        @Test
        @DisplayName("Debería lanzar exception cuando categoría no existe")
        void shouldThrowException_WhenCategoryNotFound() {
            when(repo.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.delete(999))
                    .isInstanceOf(ModelNotFoundException.class)
                    .hasMessageContaining("Categoría no encontrada ID: 999");

            verify(repo).findById(999);
            verify(repo, never()).save(any());
        }

        @Test
        @DisplayName("No debería eliminar físicamente del repositorio")
        void shouldNotDeletePhysically() {
            when(repo.findById(1)).thenReturn(Optional.of(category));
            when(repo.save(any(Category.class))).thenReturn(category);

            categoryService.delete(1);

            verify(repo, never()).deleteById(any());
            verify(repo, never()).delete(any());
        }
    }
}
