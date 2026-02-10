package com.alonso.salesapp.service.impl;

import com.alonso.salesapp.dto.ingress.IngressRequestDTO;
import com.alonso.salesapp.dto.ingress.IngressResponseDTO;
import com.alonso.salesapp.dto.ingressdetail.IngressDetailRequestDTO;
import com.alonso.salesapp.exception.ModelNotFoundException;
import com.alonso.salesapp.mapper.IngressMapper;
import com.alonso.salesapp.model.*;
import com.alonso.salesapp.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Ingress Service Tests")
class IngressServiceImplTest {

    @Mock
    private IngressRepo repo;

    @Mock
    private IngressMapper mapper;

    @Mock
    private ProviderRepo providerRepo;

    @Mock
    private UserRepo userRepo;

    @Mock
    private ProductRepo productRepo;

    @InjectMocks
    private IngressServiceImpl ingressService;

    private Ingress ingress;
    private IngressRequestDTO ingressDTO;
    private IngressResponseDTO ingressResponseDTO;
    private Provider provider;
    private User user;
    private Product product;
    private IngressDetail ingressDetail;
    private Category category;

    @BeforeEach
    void setUp() {
        // Setup Category
        category = new Category(1, "Electronics", "Electronic devices", true);

        // Setup Provider
        provider = Provider.builder()
                .idProvider(1)
                .name("Tech Supplies Inc")
                .address("123 Tech Street")
                .build();

        // Setup Role y User
        Role role = Role.builder()
                .idRole(1)
                .name("ADMIN")
                .build();

        user = User.builder()
                .idUser(1)
                .username("admin")
                .role(role)
                .build();

        // Setup Product
        product = Product.builder()
                .idProduct(1)
                .name("Laptop")
                .price(1000.0)
                .stock(5)
                .category(category)
                .build();

        // Setup IngressDetail
        ingressDetail = IngressDetail.builder()
                .idIngressDetail(1)
                .product(product)
                .quantity((short) 10)
                .cost(800.0)
                .build();

        // Setup Ingress
        ingress = Ingress.builder()
                .idIngress(1)
                .provider(provider)
                .user(user)
                .dateTime(LocalDateTime.now())
                .serialNumber("INV-001")
                .tax(1440.0)
                .total(9440.0)
                .details(new ArrayList<>(List.of(ingressDetail)))
                .build();
        ingressDetail.setIngress(ingress);

        // Setup DTOs
        IngressDetailRequestDTO detailDTO = new IngressDetailRequestDTO(null, 1, (short) 10, 800.0);
        ingressDTO = new IngressRequestDTO(null, 1, 1, "INV-001", null, null, List.of(detailDTO));
        ingressResponseDTO = mock(IngressResponseDTO.class);
    }

    @Nested
    @DisplayName("Crear Ingreso")
    class CreateTests {

        @Test
        @DisplayName("Debería crear ingreso exitosamente")
        void shouldCreateIngress_Successfully() {
            when(mapper.toEntity(ingressDTO)).thenReturn(ingress);
            when(providerRepo.findById(1)).thenReturn(Optional.of(provider));
            when(userRepo.findById(1)).thenReturn(Optional.of(user));
            when(productRepo.findByIdLocked(1)).thenReturn(Optional.of(product));
            when(repo.save(any(Ingress.class))).thenReturn(ingress);
            when(mapper.toResponseDTO(any(Ingress.class))).thenReturn(ingressResponseDTO);

            IngressResponseDTO result = ingressService.create(ingressDTO);

            assertThat(result).isNotNull();
            verify(providerRepo).findById(1);
            verify(userRepo).findById(1);
            verify(productRepo).findByIdLocked(1);
            verify(repo).save(any(Ingress.class));
        }

        @Test
        @DisplayName("Debería lanzar exception cuando proveedor no existe")
        void shouldThrowException_WhenProviderNotFound() {
            when(mapper.toEntity(ingressDTO)).thenReturn(ingress);
            when(providerRepo.findById(1)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ingressService.create(ingressDTO))
                    .isInstanceOf(ModelNotFoundException.class)
                    .hasMessageContaining("Provider not found with id: 1");

            verify(providerRepo).findById(1);
            verifyNoInteractions(userRepo, productRepo, repo);
        }

        @Test
        @DisplayName("Debería lanzar exception cuando usuario no existe")
        void shouldThrowException_WhenUserNotFound() {
            when(mapper.toEntity(ingressDTO)).thenReturn(ingress);
            when(providerRepo.findById(1)).thenReturn(Optional.of(provider));
            when(userRepo.findById(1)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ingressService.create(ingressDTO))
                    .isInstanceOf(ModelNotFoundException.class)
                    .hasMessageContaining("User not found with id: 1");

            verify(providerRepo).findById(1);
            verify(userRepo).findById(1);
            verifyNoInteractions(productRepo, repo);
        }

        @Test
        @DisplayName("Debería lanzar exception cuando producto no existe")
        void shouldThrowException_WhenProductNotFound() {
            when(mapper.toEntity(ingressDTO)).thenReturn(ingress);
            when(providerRepo.findById(1)).thenReturn(Optional.of(provider));
            when(userRepo.findById(1)).thenReturn(Optional.of(user));
            when(productRepo.findByIdLocked(1)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ingressService.create(ingressDTO))
                    .isInstanceOf(ModelNotFoundException.class)
                    .hasMessageContaining("Product not found with id: 1");

            verify(productRepo).findByIdLocked(1);
            verify(repo, never()).save(any());
        }

        @Test
        @DisplayName("Debería incrementar stock del producto")
        void shouldIncrementStock_OfProduct() {
            int initialStock = product.getStock(); // 5

            when(mapper.toEntity(ingressDTO)).thenReturn(ingress);
            when(providerRepo.findById(1)).thenReturn(Optional.of(provider));
            when(userRepo.findById(1)).thenReturn(Optional.of(user));
            when(productRepo.findByIdLocked(1)).thenReturn(Optional.of(product));
            when(repo.save(any(Ingress.class))).thenReturn(ingress);
            when(mapper.toResponseDTO(any(Ingress.class))).thenReturn(ingressResponseDTO);

            ingressService.create(ingressDTO);

            // Stock inicial: 5 + 10 = 15
            assertThat(product.getStock()).isEqualTo(initialStock + 10);
        }

        @Test
        @DisplayName("Debería calcular tax automáticamente cuando no viene en DTO")
        void shouldCalculateTax_WhenNotProvided() {
            when(mapper.toEntity(ingressDTO)).thenReturn(ingress);
            when(providerRepo.findById(1)).thenReturn(Optional.of(provider));
            when(userRepo.findById(1)).thenReturn(Optional.of(user));
            when(productRepo.findByIdLocked(1)).thenReturn(Optional.of(product));
            when(repo.save(any(Ingress.class))).thenReturn(ingress);
            when(mapper.toResponseDTO(any(Ingress.class))).thenReturn(ingressResponseDTO);

            ingressService.create(ingressDTO);

            ArgumentCaptor<Ingress> captor = ArgumentCaptor.forClass(Ingress.class);
            verify(repo).save(captor.capture());
            Ingress savedIngress = captor.getValue();

            // Subtotal: 10 * 800 = 8000
            // Tax (18%): 8000 * 0.18 = 1440
            assertThat(savedIngress.getTax()).isEqualTo(1440.0);
        }

        @Test
        @DisplayName("Debería calcular total correctamente")
        void shouldCalculateTotal_Correctly() {
            when(mapper.toEntity(ingressDTO)).thenReturn(ingress);
            when(providerRepo.findById(1)).thenReturn(Optional.of(provider));
            when(userRepo.findById(1)).thenReturn(Optional.of(user));
            when(productRepo.findByIdLocked(1)).thenReturn(Optional.of(product));
            when(repo.save(any(Ingress.class))).thenReturn(ingress);
            when(mapper.toResponseDTO(any(Ingress.class))).thenReturn(ingressResponseDTO);

            ingressService.create(ingressDTO);

            ArgumentCaptor<Ingress> captor = ArgumentCaptor.forClass(Ingress.class);
            verify(repo).save(captor.capture());
            Ingress savedIngress = captor.getValue();

            // Subtotal: 8000, Tax: 1440, Total: 9440
            assertThat(savedIngress.getTotal()).isEqualTo(9440.0);
        }

        @Test
        @DisplayName("Debería asignar fecha actual cuando no viene en DTO")
        void shouldAssignCurrentDate_WhenNotProvided() {
            LocalDateTime before = LocalDateTime.now().minusSeconds(1);

            when(mapper.toEntity(ingressDTO)).thenReturn(ingress);
            when(providerRepo.findById(1)).thenReturn(Optional.of(provider));
            when(userRepo.findById(1)).thenReturn(Optional.of(user));
            when(productRepo.findByIdLocked(1)).thenReturn(Optional.of(product));
            when(repo.save(any(Ingress.class))).thenReturn(ingress);
            when(mapper.toResponseDTO(any(Ingress.class))).thenReturn(ingressResponseDTO);

            ingressService.create(ingressDTO);

            LocalDateTime after = LocalDateTime.now().plusSeconds(1);
            ArgumentCaptor<Ingress> captor = ArgumentCaptor.forClass(Ingress.class);
            verify(repo).save(captor.capture());
            Ingress savedIngress = captor.getValue();

            assertThat(savedIngress.getDateTime()).isBetween(before, after);
        }

        @Test
        @DisplayName("Debería lanzar exception cuando el ingreso no tiene detalles")
        void shouldThrowException_WhenIngressHasNoDetails() {
            Ingress ingressWithoutDetails = new Ingress();
            ingressWithoutDetails.setDetails(new ArrayList<>());

            when(mapper.toEntity(ingressDTO)).thenReturn(ingressWithoutDetails);

            assertThatThrownBy(() -> ingressService.create(ingressDTO))
                    .isInstanceOf(ModelNotFoundException.class)
                    .hasMessageContaining("No se puede registrar un ingreso sin detalles");

            verifyNoInteractions(providerRepo, userRepo, productRepo, repo);
        }

        @Test
        @DisplayName("Debería lanzar exception cuando details es null")
        void shouldThrowException_WhenDetailsIsNull() {
            Ingress ingressWithNullDetails = new Ingress();
            ingressWithNullDetails.setDetails(null);

            when(mapper.toEntity(ingressDTO)).thenReturn(ingressWithNullDetails);

            assertThatThrownBy(() -> ingressService.create(ingressDTO))
                    .isInstanceOf(ModelNotFoundException.class)
                    .hasMessageContaining("No se puede registrar un ingreso sin detalles");
        }

        @Test
        @DisplayName("Debería usar tax proporcionado en lugar de calcularlo")
        void shouldUseTaxProvided_InsteadOfCalculating() {
            IngressRequestDTO dtoWithTax = new IngressRequestDTO(
                    null, 1, 1, "INV-001", null, 500.0, ingressDTO.details()
            );
            Ingress ingressWithTax = new Ingress();
            ingressWithTax.setDetails(new ArrayList<>(List.of(ingressDetail)));
            ingressDetail.setIngress(ingressWithTax);

            when(mapper.toEntity(dtoWithTax)).thenReturn(ingressWithTax);
            when(providerRepo.findById(1)).thenReturn(Optional.of(provider));
            when(userRepo.findById(1)).thenReturn(Optional.of(user));
            when(productRepo.findByIdLocked(1)).thenReturn(Optional.of(product));
            when(repo.save(any(Ingress.class))).thenReturn(ingressWithTax);
            when(mapper.toResponseDTO(any(Ingress.class))).thenReturn(ingressResponseDTO);

            ingressService.create(dtoWithTax);

            ArgumentCaptor<Ingress> captor = ArgumentCaptor.forClass(Ingress.class);
            verify(repo).save(captor.capture());
            assertThat(captor.getValue().getTax()).isEqualTo(500.0);
        }

        @Test
        @DisplayName("Debería establecer la referencia bidireccional en detalles")
        void shouldSetBidirectionalReference_InDetails() {
            when(mapper.toEntity(ingressDTO)).thenReturn(ingress);
            when(providerRepo.findById(1)).thenReturn(Optional.of(provider));
            when(userRepo.findById(1)).thenReturn(Optional.of(user));
            when(productRepo.findByIdLocked(1)).thenReturn(Optional.of(product));
            when(repo.save(any(Ingress.class))).thenReturn(ingress);
            when(mapper.toResponseDTO(any(Ingress.class))).thenReturn(ingressResponseDTO);

            ingressService.create(ingressDTO);

            ArgumentCaptor<Ingress> captor = ArgumentCaptor.forClass(Ingress.class);
            verify(repo).save(captor.capture());
            Ingress savedIngress = captor.getValue();

            savedIngress.getDetails().forEach(detail -> {
                assertThat(detail.getIngress()).isEqualTo(savedIngress);
            });
        }

        @Test
        @DisplayName("Debería manejar múltiples productos en el ingreso")
        void shouldHandleMultipleProducts_InIngress() {
            Product product2 = new Product();
            product2.setIdProduct(2);
            product2.setName("Mouse");
            product2.setStock(20);

            IngressDetail detail2 = new IngressDetail();
            detail2.setIdIngressDetail(2);
            detail2.setProduct(product2);
            detail2.setQuantity((short) 5);
            detail2.setCost(50.0);

            ingress.getDetails().add(detail2);
            detail2.setIngress(ingress);

            when(mapper.toEntity(ingressDTO)).thenReturn(ingress);
            when(providerRepo.findById(1)).thenReturn(Optional.of(provider));
            when(userRepo.findById(1)).thenReturn(Optional.of(user));
            when(productRepo.findByIdLocked(1)).thenReturn(Optional.of(product));
            when(productRepo.findByIdLocked(2)).thenReturn(Optional.of(product2));
            when(repo.save(any(Ingress.class))).thenReturn(ingress);
            when(mapper.toResponseDTO(any(Ingress.class))).thenReturn(ingressResponseDTO);

            ingressService.create(ingressDTO);

            verify(productRepo).findByIdLocked(1);
            verify(productRepo).findByIdLocked(2);
            assertThat(product.getStock()).isEqualTo(15); // 5 + 10
            assertThat(product2.getStock()).isEqualTo(25); // 20 + 5
        }
    }

    @Nested
    @DisplayName("Consultar Ingresos")
    class ReadTests {

        @Test
        @DisplayName("Debería retornar todos los ingresos")
        void shouldReturnAllIngresses() {
            when(repo.findAll()).thenReturn(List.of(ingress));
            when(mapper.toResponseDTOList(anyList())).thenReturn(List.of(ingressResponseDTO));

            List<IngressResponseDTO> result = ingressService.readAll();

            assertThat(result).hasSize(1);
            verify(repo).findAll();
            verify(mapper).toResponseDTOList(anyList());
        }

        @Test
        @DisplayName("Debería retornar lista vacía cuando no hay ingresos")
        void shouldReturnEmptyList_WhenNoIngressesExist() {
            when(repo.findAll()).thenReturn(List.of());
            when(mapper.toResponseDTOList(anyList())).thenReturn(List.of());

            List<IngressResponseDTO> result = ingressService.readAll();

            assertThat(result).isEmpty();
            verify(repo).findAll();
        }

        @Test
        @DisplayName("Debería retornar ingreso por ID")
        void shouldReturnIngress_ById() {
            when(repo.findById(1)).thenReturn(Optional.of(ingress));
            when(mapper.toResponseDTO(ingress)).thenReturn(ingressResponseDTO);

            IngressResponseDTO result = ingressService.readById(1);

            assertThat(result).isNotNull();
            verify(repo).findById(1);
        }

        @Test
        @DisplayName("Debería lanzar exception cuando ingreso no existe")
        void shouldThrowException_WhenIngressNotFound() {
            when(repo.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ingressService.readById(999))
                    .isInstanceOf(ModelNotFoundException.class)
                    .hasMessageContaining("Sale not found with id: 999");

            verify(repo).findById(999);
        }
    }
}
