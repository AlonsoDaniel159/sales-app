package com.alonso.salesapp.repository;

import com.alonso.salesapp.model.Category;
import com.alonso.salesapp.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class ProductRepoTest {

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private TestEntityManager entityManager;

    private Category category;
    private Product product;

    @BeforeEach
    void setUp() {
        // Configuración común para cada test, si es necesario
        category = Category.builder()
                .name("Electronics")
                .description("Electronic devices")
                .enabled(true)
                .build();
        entityManager.persist(category);

        // Crear producto
        product = Product.builder()
                .name("Laptop")
                .description("Gaming laptop")
                .price(1500.0)
                .stock(10)
                .category(category)
                .enabled(true)
                .build();

        entityManager.persist(product);
        entityManager.flush();
    }

    @Test
    @Transactional
    public void testFindByIdLocked() {
        // Crear y persistir un producto de prueba


        // Ejecutar la consulta con bloqueo
        Optional<Product> result = productRepo.findByIdLocked(1);

        // Verificar que el producto se encuentre y tenga los datos correctos
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Laptop");
    }

    @Test
    public void testFindByIdLockedNotFound() {
        // Intentar encontrar un producto que no existe
        Optional<Product> result = productRepo.findByIdLocked(999);

        // Verificar que no se encuentre
        assertThat(result).isEmpty();
    }
}