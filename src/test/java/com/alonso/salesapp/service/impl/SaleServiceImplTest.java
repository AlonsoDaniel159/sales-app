package com.alonso.salesapp.service.impl;

import com.alonso.salesapp.dto.sale.SaleDTO;
import com.alonso.salesapp.dto.sale.SaleDetailDTO;
import com.alonso.salesapp.dto.sale.SaleResponseDTO;
import com.alonso.salesapp.exception.ModelNotFoundException;
import com.alonso.salesapp.mapper.SaleMapper;
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
@DisplayName("Sale Service Tests")
class SaleServiceImplTest {

    @Mock
    private SaleRepo repo;

    @Mock
    private SaleMapper mapper;

    @Mock
    private ClientRepo clientRepo;

    @Mock
    private UserRepo userRepo;

    @Mock
    private ProductRepo productRepo;

    @InjectMocks
    private SaleServiceImpl saleService;

    private Sale sale;
    private SaleDTO saleDTO;
    private SaleResponseDTO saleResponseDTO;
    private Client client;
    private User user;
    private Product product;
    private SaleDetail saleDetail;

    @BeforeEach
    void setUp() {
        // Setup Category
        Category category = new Category(1, "Electronics", "Electronic devices", true);

        // Setup Client
        client = new Client();
        client.setIdClient(1);
        client.setFirstName("John");
        client.setLastName("Doe");

        // Setup Role y User
        Role role = new Role();
        role.setIdRole(1);
        role.setName("SELLER");

        user = new User();
        user.setIdUser(1);
        user.setUsername("seller");
        user.setRole(role);

        // Setup Product
        product = new Product();
        product.setIdProduct(1);
        product.setName("Laptop");
        product.setPrice(1000.0);
        product.setStock(10);
        product.setCategory(category);

        // Setup SaleDetail
        saleDetail = new SaleDetail();
        saleDetail.setIdSaleDetail(1);
        saleDetail.setProduct(product);
        saleDetail.setQuantity((short) 2);
        saleDetail.setSalePrice(1000.0);
        saleDetail.setDiscount(0.0);

        // Setup Sale
        sale = new Sale();
        sale.setIdSale(1);
        sale.setClient(client);
        sale.setUser(user);
        sale.setDateTime(LocalDateTime.now());
        sale.setTax(360.0);
        sale.setTotal(2360.0);
        sale.setDetails(new ArrayList<>(List.of(saleDetail)));
        saleDetail.setSale(sale);

        // Setup DTOs
        SaleDetailDTO detailDTO = new SaleDetailDTO(null, 1, (short) 2, 0.0, null);
        saleDTO = new SaleDTO(null, 1, 1, null, null, List.of(detailDTO));
        saleResponseDTO = mock(SaleResponseDTO.class);
    }

    @Nested
    @DisplayName("Crear Venta")
    class CreateTests {

        @Test
        @DisplayName("Debería crear venta exitosamente")
        void shouldCreateSale_Successfully() {
            when(mapper.toEntity(saleDTO)).thenReturn(sale);
            when(clientRepo.findById(1)).thenReturn(Optional.of(client));
            when(userRepo.findById(1)).thenReturn(Optional.of(user));
            when(productRepo.findByIdLocked(1)).thenReturn(Optional.of(product));
            when(repo.save(any(Sale.class))).thenReturn(sale);
            when(mapper.toResponseDTO(any(Sale.class))).thenReturn(saleResponseDTO);

            SaleResponseDTO result = saleService.create(saleDTO);

            assertThat(result).isNotNull();
            verify(clientRepo).findById(1);
            verify(userRepo).findById(1);
            verify(productRepo).findByIdLocked(1);
            verify(repo).save(any(Sale.class));
        }

        @Test
        @DisplayName("Debería lanzar exception cuando cliente no existe")
        void shouldThrowException_WhenClientNotFound() {
            when(mapper.toEntity(saleDTO)).thenReturn(sale);
            when(clientRepo.findById(1)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> saleService.create(saleDTO))
                    .isInstanceOf(ModelNotFoundException.class)
                    .hasMessageContaining("Client not found with id: 1");

            verify(clientRepo).findById(1);
            verifyNoInteractions(userRepo, productRepo, repo);
        }

        @Test
        @DisplayName("Debería lanzar exception cuando usuario no existe")
        void shouldThrowException_WhenUserNotFound() {
            when(mapper.toEntity(saleDTO)).thenReturn(sale);
            when(clientRepo.findById(1)).thenReturn(Optional.of(client));
            when(userRepo.findById(1)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> saleService.create(saleDTO))
                    .isInstanceOf(ModelNotFoundException.class)
                    .hasMessageContaining("User not found with id: 1");

            verify(clientRepo).findById(1);
            verify(userRepo).findById(1);
            verifyNoInteractions(productRepo, repo);
        }

        @Test
        @DisplayName("Debería lanzar exception cuando producto no existe")
        void shouldThrowException_WhenProductNotFound() {
            when(mapper.toEntity(saleDTO)).thenReturn(sale);
            when(clientRepo.findById(1)).thenReturn(Optional.of(client));
            when(userRepo.findById(1)).thenReturn(Optional.of(user));
            when(productRepo.findByIdLocked(1)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> saleService.create(saleDTO))
                    .isInstanceOf(ModelNotFoundException.class)
                    .hasMessageContaining("Product not found with id: 1");

            verify(productRepo).findByIdLocked(1);
            verify(repo, never()).save(any());
        }

        @Test
        @DisplayName("Debería lanzar exception cuando no hay stock suficiente")
        void shouldThrowException_WhenInsufficientStock() {
            product.setStock(1); // Stock menor a la cantidad requerida (2)

            when(mapper.toEntity(saleDTO)).thenReturn(sale);
            when(clientRepo.findById(1)).thenReturn(Optional.of(client));
            when(userRepo.findById(1)).thenReturn(Optional.of(user));
            when(productRepo.findByIdLocked(1)).thenReturn(Optional.of(product));

            assertThatThrownBy(() -> saleService.create(saleDTO))
                    .isInstanceOf(ModelNotFoundException.class)
                    .hasMessageContaining("Stock insuficiente para el producto: Laptop");

            verify(repo, never()).save(any());
        }

        @Test
        @DisplayName("Debería descontar stock del producto")
        void shouldDeductStock_FromProduct() {
            int initialStock = product.getStock();

            when(mapper.toEntity(saleDTO)).thenReturn(sale);
            when(clientRepo.findById(1)).thenReturn(Optional.of(client));
            when(userRepo.findById(1)).thenReturn(Optional.of(user));
            when(productRepo.findByIdLocked(1)).thenReturn(Optional.of(product));
            when(repo.save(any(Sale.class))).thenReturn(sale);
            when(mapper.toResponseDTO(any(Sale.class))).thenReturn(saleResponseDTO);

            saleService.create(saleDTO);

            assertThat(product.getStock()).isEqualTo(initialStock - 2);
        }

        @Test
        @DisplayName("Debería calcular tax automáticamente cuando no viene en DTO")
        void shouldCalculateTax_WhenNotProvided() {
            when(mapper.toEntity(saleDTO)).thenReturn(sale);
            when(clientRepo.findById(1)).thenReturn(Optional.of(client));
            when(userRepo.findById(1)).thenReturn(Optional.of(user));
            when(productRepo.findByIdLocked(1)).thenReturn(Optional.of(product));
            when(repo.save(any(Sale.class))).thenReturn(sale);
            when(mapper.toResponseDTO(any(Sale.class))).thenReturn(saleResponseDTO);

            saleService.create(saleDTO);

            ArgumentCaptor<Sale> captor = ArgumentCaptor.forClass(Sale.class);
            verify(repo).save(captor.capture());
            Sale savedSale = captor.getValue();

            // Subtotal: 2 * 1000 - 0 = 2000
            // Tax (18%): 2000 * 0.18 = 360
            assertThat(savedSale.getTax()).isEqualTo(360.0);
        }

        @Test
        @DisplayName("Debería calcular total correctamente")
        void shouldCalculateTotal_Correctly() {
            when(mapper.toEntity(saleDTO)).thenReturn(sale);
            when(clientRepo.findById(1)).thenReturn(Optional.of(client));
            when(userRepo.findById(1)).thenReturn(Optional.of(user));
            when(productRepo.findByIdLocked(1)).thenReturn(Optional.of(product));
            when(repo.save(any(Sale.class))).thenReturn(sale);
            when(mapper.toResponseDTO(any(Sale.class))).thenReturn(saleResponseDTO);

            saleService.create(saleDTO);

            ArgumentCaptor<Sale> captor = ArgumentCaptor.forClass(Sale.class);
            verify(repo).save(captor.capture());
            Sale savedSale = captor.getValue();

            // Subtotal: 2000, Tax: 360, Total: 2360
            assertThat(savedSale.getTotal()).isEqualTo(2360.0);
        }

        @Test
        @DisplayName("Debería asignar fecha actual cuando no viene en DTO")
        void shouldAssignCurrentDate_WhenNotProvided() {
            LocalDateTime before = LocalDateTime.now().minusSeconds(1);

            when(mapper.toEntity(saleDTO)).thenReturn(sale);
            when(clientRepo.findById(1)).thenReturn(Optional.of(client));
            when(userRepo.findById(1)).thenReturn(Optional.of(user));
            when(productRepo.findByIdLocked(1)).thenReturn(Optional.of(product));
            when(repo.save(any(Sale.class))).thenReturn(sale);
            when(mapper.toResponseDTO(any(Sale.class))).thenReturn(saleResponseDTO);

            saleService.create(saleDTO);

            LocalDateTime after = LocalDateTime.now().plusSeconds(1);
            ArgumentCaptor<Sale> captor = ArgumentCaptor.forClass(Sale.class);
            verify(repo).save(captor.capture());
            Sale savedSale = captor.getValue();

            assertThat(savedSale.getDateTime()).isBetween(before, after);
        }

        @Test
        @DisplayName("Debería lanzar exception cuando la venta no tiene detalles")
        void shouldThrowException_WhenSaleHasNoDetails() {
            Sale saleWithoutDetails = new Sale();
            saleWithoutDetails.setDetails(new ArrayList<>());

            when(mapper.toEntity(saleDTO)).thenReturn(saleWithoutDetails);

            assertThatThrownBy(() -> saleService.create(saleDTO))
                    .isInstanceOf(ModelNotFoundException.class)
                    .hasMessageContaining("No se puede registrar una venta sin detalles");

            verifyNoInteractions(clientRepo, userRepo, productRepo, repo);
        }

        @Test
        @DisplayName("Debería lanzar exception cuando details es null")
        void shouldThrowException_WhenDetailsIsNull() {
            Sale saleWithNullDetails = new Sale();
            saleWithNullDetails.setDetails(null);

            when(mapper.toEntity(saleDTO)).thenReturn(saleWithNullDetails);

            assertThatThrownBy(() -> saleService.create(saleDTO))
                    .isInstanceOf(ModelNotFoundException.class)
                    .hasMessageContaining("No se puede registrar una venta sin detalles");
        }

        @Test
        @DisplayName("Debería usar tax proporcionado en lugar de calcularlo")
        void shouldUseTaxProvided_InsteadOfCalculating() {
            SaleDTO dtoWithTax = new SaleDTO(null, 1, 1, null, 100.0, saleDTO.details());
            Sale saleWithTax = new Sale();
            saleWithTax.setDetails(new ArrayList<>(List.of(saleDetail)));
            saleDetail.setSale(saleWithTax);

            when(mapper.toEntity(dtoWithTax)).thenReturn(saleWithTax);
            when(clientRepo.findById(1)).thenReturn(Optional.of(client));
            when(userRepo.findById(1)).thenReturn(Optional.of(user));
            when(productRepo.findByIdLocked(1)).thenReturn(Optional.of(product));
            when(repo.save(any(Sale.class))).thenReturn(saleWithTax);
            when(mapper.toResponseDTO(any(Sale.class))).thenReturn(saleResponseDTO);

            saleService.create(dtoWithTax);

            ArgumentCaptor<Sale> captor = ArgumentCaptor.forClass(Sale.class);
            verify(repo).save(captor.capture());
            assertThat(captor.getValue().getTax()).isEqualTo(100.0);
        }

        @Test
        @DisplayName("Debería establecer la referencia bidireccional en detalles")
        void shouldSetBidirectionalReference_InDetails() {
            when(mapper.toEntity(saleDTO)).thenReturn(sale);
            when(clientRepo.findById(1)).thenReturn(Optional.of(client));
            when(userRepo.findById(1)).thenReturn(Optional.of(user));
            when(productRepo.findByIdLocked(1)).thenReturn(Optional.of(product));
            when(repo.save(any(Sale.class))).thenReturn(sale);
            when(mapper.toResponseDTO(any(Sale.class))).thenReturn(saleResponseDTO);

            saleService.create(saleDTO);

            ArgumentCaptor<Sale> captor = ArgumentCaptor.forClass(Sale.class);
            verify(repo).save(captor.capture());
            Sale savedSale = captor.getValue();

            savedSale.getDetails().forEach(detail -> assertThat(detail.getSale()).isEqualTo(savedSale));
        }
    }

    @Nested
    @DisplayName("Consultar Ventas")
    class ReadTests {

        @Test
        @DisplayName("Debería retornar todas las ventas")
        void shouldReturnAllSales() {
            when(repo.findAll()).thenReturn(List.of(sale));
            when(mapper.toResponseDTOList(anyList())).thenReturn(List.of(saleResponseDTO));

            List<SaleResponseDTO> result = saleService.readAll();

            assertThat(result).hasSize(1);
            verify(repo).findAll();
            verify(mapper).toResponseDTOList(anyList());
        }

        @Test
        @DisplayName("Debería retornar lista vacía cuando no hay ventas")
        void shouldReturnEmptyList_WhenNoSalesExist() {
            when(repo.findAll()).thenReturn(List.of());
            when(mapper.toResponseDTOList(anyList())).thenReturn(List.of());

            List<SaleResponseDTO> result = saleService.readAll();

            assertThat(result).isEmpty();
            verify(repo).findAll();
        }

        @Test
        @DisplayName("Debería retornar venta por ID")
        void shouldReturnSale_ById() {
            when(repo.findById(1)).thenReturn(Optional.of(sale));
            when(mapper.toResponseDTO(sale)).thenReturn(saleResponseDTO);

            SaleResponseDTO result = saleService.readById(1);

            assertThat(result).isNotNull();
            verify(repo).findById(1);
        }

        @Test
        @DisplayName("Debería lanzar exception cuando venta no existe")
        void shouldThrowException_WhenSaleNotFound() {
            when(repo.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> saleService.readById(999))
                    .isInstanceOf(ModelNotFoundException.class)
                    .hasMessageContaining("Sale not found with id: 999");

            verify(repo).findById(999);
        }
    }
}
