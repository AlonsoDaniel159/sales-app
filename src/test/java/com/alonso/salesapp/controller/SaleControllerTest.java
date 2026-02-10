package com.alonso.salesapp.controller;

import com.alonso.salesapp.dto.client.ClientSummaryDTO;
import com.alonso.salesapp.dto.product.ProductSummaryDTO;
import com.alonso.salesapp.dto.sale.SaleDTO;
import com.alonso.salesapp.dto.sale.SaleDetailDTO;
import com.alonso.salesapp.dto.sale.SaleDetailResponseDTO;
import com.alonso.salesapp.dto.sale.SaleResponseDTO;
import com.alonso.salesapp.dto.user.UserSummaryDTO;
import com.alonso.salesapp.exception.ModelNotFoundException;
import com.alonso.salesapp.service.ISaleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SaleController.class)
class SaleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ISaleService saleService;

    @Test
    @DisplayName("Debería retornar todas las ventas cuando se llama a GET /sales")
    void shouldReturnAllSales_whenGetAllSalesIsCalled() throws Exception {
        ClientSummaryDTO client = new ClientSummaryDTO(1, "John", "Doe");
        UserSummaryDTO user = new UserSummaryDTO(1, "admin");
        ProductSummaryDTO product = new ProductSummaryDTO(1, "Laptop", 1500.00);
        SaleDetailResponseDTO detail = new SaleDetailResponseDTO(product, (short) 2, 1500.00, 0.0);

        SaleResponseDTO sale1 = new SaleResponseDTO(1, client, user, LocalDateTime.now(),
            3000.00, 540.00, List.of(detail));
        SaleResponseDTO sale2 = new SaleResponseDTO(2, client, user, LocalDateTime.now(),
            1500.00, 270.00, List.of(detail));

        List<SaleResponseDTO> sales = List.of(sale1, sale2);

        when(saleService.readAll()).thenReturn(sales);

        mockMvc.perform(get("/sales")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].idSale").value(1))
                .andExpect(jsonPath("$[0].total").value(3000.00))
                .andExpect(jsonPath("$[1].idSale").value(2))
                .andExpect(jsonPath("$[1].total").value(1500.00));
    }

    @Test
    @DisplayName("Debería retornar una venta cuando se proporciona un ID válido")
    void shouldReturnSale_whenValidIdIsProvided() throws Exception {
        ClientSummaryDTO client = new ClientSummaryDTO(1, "John", "Doe");
        UserSummaryDTO user = new UserSummaryDTO(1, "admin");
        ProductSummaryDTO product = new ProductSummaryDTO(1, "Laptop", 1500.00);
        SaleDetailResponseDTO detail = new SaleDetailResponseDTO(product, (short) 2, 1500.00, 0.0);

        SaleResponseDTO sale = new SaleResponseDTO(1, client, user, LocalDateTime.now(),
            3000.00, 540.00, List.of(detail));

        when(saleService.readById(1)).thenReturn(sale);

        mockMvc.perform(get("/sales/{idSale}", 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.idSale").value(1))
                .andExpect(jsonPath("$.total").value(3000.00))
                .andExpect(jsonPath("$.client.idClient").value(1))
                .andExpect(jsonPath("$.user.idUser").value(1));
    }

    @Test
    @DisplayName("Debería crear una venta cuando se proporcionan datos válidos")
    void shouldCreateSale_whenValidDataIsProvided() throws Exception {
        SaleDetailDTO detail = new SaleDetailDTO(null, 1, (short) 2, 1500.00, 0.0);
        SaleDTO inputDTO = new SaleDTO(null, 1, 1, LocalDateTime.now(), 540.00, List.of(detail));

        ClientSummaryDTO client = new ClientSummaryDTO(1, "John", "Doe");
        UserSummaryDTO user = new UserSummaryDTO(1, "admin");
        ProductSummaryDTO product = new ProductSummaryDTO(1, "Laptop", 1500.00);
        SaleDetailResponseDTO responseDetail = new SaleDetailResponseDTO(product, (short) 2, 1500.00, 0.0);
        SaleResponseDTO responseDTO = new SaleResponseDTO(1, client, user, LocalDateTime.now(),
            3000.00, 540.00, List.of(responseDetail));

        when(saleService.create(any(SaleDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.idSale").value(1))
                .andExpect(jsonPath("$.total").value(3000.00));
    }

    @Test
    @DisplayName("Debería retornar Bad Request cuando el client ID es null")
    void shouldReturnBadRequest_whenClientIdIsNull() throws Exception {
        SaleDetailDTO detail = new SaleDetailDTO(null, 1, (short) 2, 1500.00, 0.0);
        SaleDTO invalidDTO = new SaleDTO(null, null, 1, LocalDateTime.now(), 540.00, List.of(detail));

        mockMvc.perform(post("/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.idClient").exists());
    }

    @Test
    @DisplayName("Debería retornar Bad Request cuando el client ID es menor a 1")
    void shouldReturnBadRequest_whenClientIdIsLessThanOne() throws Exception {
        SaleDetailDTO detail = new SaleDetailDTO(null, 1, (short) 2, 1500.00, 0.0);
        SaleDTO invalidDTO = new SaleDTO(null, 0, 1, LocalDateTime.now(), 540.00, List.of(detail));

        mockMvc.perform(post("/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.idClient").value("Client ID must be valid"));
    }

    @Test
    @DisplayName("Debería retornar Bad Request cuando el user ID es null")
    void shouldReturnBadRequest_whenUserIdIsNull() throws Exception {
        SaleDetailDTO detail = new SaleDetailDTO(null, 1, (short) 2, 1500.00, 0.0);
        SaleDTO invalidDTO = new SaleDTO(null, 1, null, LocalDateTime.now(), 540.00, List.of(detail));

        mockMvc.perform(post("/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.idUser").exists());
    }

    @Test
    @DisplayName("Debería retornar Bad Request cuando la lista de detalles está vacía")
    void shouldReturnBadRequest_whenDetailsListIsEmpty() throws Exception {
        SaleDTO invalidDTO = new SaleDTO(null, 1, 1, LocalDateTime.now(), 540.00, List.of());

        mockMvc.perform(post("/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.details").value("La venta debe tener al menos un producto"));
    }

    @Test
    @DisplayName("Debería retornar Bad Request cuando la lista de detalles es null")
    void shouldReturnBadRequest_whenDetailsListIsNull() throws Exception {
        SaleDTO invalidDTO = new SaleDTO(null, 1, 1, LocalDateTime.now(), 540.00, null);

        mockMvc.perform(post("/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.details").exists());
    }

    @Test
    @DisplayName("Debería retornar Not Found cuando la venta no existe")
    void shouldReturnNotFound_whenSaleDoesNotExist() throws Exception {
        when(saleService.readById(999)).thenThrow(new ModelNotFoundException("Sale not found"));

        mockMvc.perform(get("/sales/{idSale}", 999)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Debería retornar lista vacía cuando no hay ventas")
    void shouldReturnEmptyList_whenNoSalesExist() throws Exception {
        when(saleService.readAll()).thenReturn(List.of());

        mockMvc.perform(get("/sales")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Debería retornar Bad Request cuando el product ID en detalle es null")
    void shouldReturnBadRequest_whenDetailProductIdIsNull() throws Exception {
        SaleDetailDTO detail = new SaleDetailDTO(null, null, (short) 2, 1500.00, 0.0);
        SaleDTO invalidDTO = new SaleDTO(null, 1, 1, LocalDateTime.now(), 540.00, List.of(detail));

        mockMvc.perform(post("/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['details[0].idProduct']").exists());
    }

    @Test
    @DisplayName("Debería retornar Bad Request cuando la cantidad en detalle es null")
    void shouldReturnBadRequest_whenDetailQuantityIsNull() throws Exception {
        SaleDetailDTO detail = new SaleDetailDTO(null, 1, null, 1500.00, 0.0);
        SaleDTO invalidDTO = new SaleDTO(null, 1, 1, LocalDateTime.now(), 540.00, List.of(detail));

        mockMvc.perform(post("/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['details[0].quantity']").exists());
    }
}

