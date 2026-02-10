package com.alonso.salesapp.controller;

import com.alonso.salesapp.dto.ingress.IngressRequestDTO;
import com.alonso.salesapp.dto.ingress.IngressResponseDTO;
import com.alonso.salesapp.dto.ingressdetail.IngressDetailRequestDTO;
import com.alonso.salesapp.dto.ingressdetail.IngressDetailResponseDTO;
import com.alonso.salesapp.dto.product.ProductSummaryDTO;
import com.alonso.salesapp.dto.provider.ProviderSummaryDTO;
import com.alonso.salesapp.dto.user.UserSummaryDTO;
import com.alonso.salesapp.exception.ModelNotFoundException;
import com.alonso.salesapp.service.IngressService;
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

@WebMvcTest(IngressController.class)
class IngressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IngressService ingressService;

    @Test
    @DisplayName("Debería retornar todos los ingresos cuando se llama a GET /ingress")
    void shouldReturnAllIngresses_whenGetAllIngressesIsCalled() throws Exception {
        ProviderSummaryDTO provider = new ProviderSummaryDTO(1, "Tech Supplier", "123 Tech St");
        UserSummaryDTO user = new UserSummaryDTO(1, "admin");
        ProductSummaryDTO product = new ProductSummaryDTO(1, "Laptop", 0.00);
        IngressDetailResponseDTO detail = new IngressDetailResponseDTO(1, product, (short) 10, 1500.00);

        IngressResponseDTO ingress1 = new IngressResponseDTO(1, provider, user, "INV-001",
            LocalDateTime.now(), 15000.00, 2700.00, List.of(detail));
        IngressResponseDTO ingress2 = new IngressResponseDTO(2, provider, user, "INV-002",
            LocalDateTime.now(), 8000.00, 1440.00, List.of(detail));

        List<IngressResponseDTO> ingresses = List.of(ingress1, ingress2);

        when(ingressService.readAll()).thenReturn(ingresses);

        mockMvc.perform(get("/ingress")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].idIngress").value(1))
                .andExpect(jsonPath("$[0].serialNumber").value("INV-001"))
                .andExpect(jsonPath("$[1].idIngress").value(2))
                .andExpect(jsonPath("$[1].serialNumber").value("INV-002"));
    }

    @Test
    @DisplayName("Debería retornar un ingreso cuando se proporciona un ID válido")
    void shouldReturnIngress_whenValidIdIsProvided() throws Exception {
        ProviderSummaryDTO provider = new ProviderSummaryDTO(1, "Tech Supplier", "123 Tech St");
        UserSummaryDTO user = new UserSummaryDTO(1, "admin");
        ProductSummaryDTO product = new ProductSummaryDTO(1, "Laptop", 0.00);
        IngressDetailResponseDTO detail = new IngressDetailResponseDTO(1, product, (short) 10, 1500.00);

        IngressResponseDTO ingress = new IngressResponseDTO(1, provider, user, "INV-001",
            LocalDateTime.now(), 15000.00, 2700.00, List.of(detail));

        when(ingressService.readById(1)).thenReturn(ingress);

        mockMvc.perform(get("/ingress/{idIngress}", 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.idIngress").value(1))
                .andExpect(jsonPath("$.serialNumber").value("INV-001"))
                .andExpect(jsonPath("$.provider.idProvider").value(1))
                .andExpect(jsonPath("$.user.idUser").value(1));
    }

    @Test
    @DisplayName("Debería crear un ingreso cuando se proporcionan datos válidos")
    void shouldCreateIngress_whenValidDataIsProvided() throws Exception {
        IngressDetailRequestDTO detail = new IngressDetailRequestDTO(null, 1, (short) 10, 1500.00);
        IngressRequestDTO inputDTO = new IngressRequestDTO(null, 1, 1, "INV-001",
            LocalDateTime.now(), 2700.00, List.of(detail));

        ProviderSummaryDTO provider = new ProviderSummaryDTO(1, "Tech Supplier", "123 Tech St");
        UserSummaryDTO user = new UserSummaryDTO(1, "admin");
        ProductSummaryDTO product = new ProductSummaryDTO(1, "Laptop", 0.00);
        IngressDetailResponseDTO responseDetail = new IngressDetailResponseDTO(1, product, (short) 10, 1500.00);
        IngressResponseDTO responseDTO = new IngressResponseDTO(1, provider, user, "INV-001",
            LocalDateTime.now(), 15000.00, 2700.00, List.of(responseDetail));

        when(ingressService.create(any(IngressRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/ingress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.idIngress").value(1))
                .andExpect(jsonPath("$.serialNumber").value("INV-001"))
                .andExpect(jsonPath("$.total").value(15000.00));
    }

    @Test
    @DisplayName("Debería retornar Bad Request cuando el provider ID es null")
    void shouldReturnBadRequest_whenProviderIdIsNull() throws Exception {
        IngressDetailRequestDTO detail = new IngressDetailRequestDTO(null, 1, (short) 10, 1500.00);
        IngressRequestDTO invalidDTO = new IngressRequestDTO(null, null, 1, "INV-001",
            LocalDateTime.now(), 2700.00, List.of(detail));

        mockMvc.perform(post("/ingress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.idProvider").exists());
    }

    @Test
    @DisplayName("Debería retornar Bad Request cuando el provider ID es menor a 1")
    @SuppressWarnings("ConstantConditions")
    void shouldReturnBadRequest_whenProviderIdIsLessThanOne() throws Exception {
        IngressDetailRequestDTO detail = new IngressDetailRequestDTO(null, 1, (short) 10, 1500.00);
        IngressRequestDTO invalidDTO = new IngressRequestDTO(null, 0, 1, "INV-001",
            LocalDateTime.now(), 2700.00, List.of(detail));

        mockMvc.perform(post("/ingress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.idProvider").value("Provider ID must be valid"));
    }

    @Test
    @DisplayName("Debería retornar Bad Request cuando el user ID es null")
    void shouldReturnBadRequest_whenUserIdIsNull() throws Exception {
        IngressDetailRequestDTO detail = new IngressDetailRequestDTO(null, 1, (short) 10, 1500.00);
        IngressRequestDTO invalidDTO = new IngressRequestDTO(null, 1, null, "INV-001",
            LocalDateTime.now(), 2700.00, List.of(detail));

        mockMvc.perform(post("/ingress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.idUser").exists());
    }

    @Test
    @DisplayName("Debería retornar Bad Request cuando el serial number es null")
    void shouldReturnBadRequest_whenSerialNumberIsNull() throws Exception {
        IngressDetailRequestDTO detail = new IngressDetailRequestDTO(null, 1, (short) 10, 1500.00);
        IngressRequestDTO invalidDTO = new IngressRequestDTO(null, 1, 1, null,
            LocalDateTime.now(), 2700.00, List.of(detail));

        mockMvc.perform(post("/ingress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.serialNumber").exists());
    }

    @Test
    @DisplayName("Debería retornar Bad Request cuando la lista de detalles está vacía")
    void shouldReturnBadRequest_whenDetailsListIsEmpty() throws Exception {
        IngressRequestDTO invalidDTO = new IngressRequestDTO(null, 1, 1, "INV-001",
            LocalDateTime.now(), 2700.00, List.of());

        mockMvc.perform(post("/ingress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.details").value("El ingreso debe tener al menos un producto"));
    }

    @Test
    @DisplayName("Debería retornar Bad Request cuando la lista de detalles es null")
    void shouldReturnBadRequest_whenDetailsListIsNull() throws Exception {
        IngressRequestDTO invalidDTO = new IngressRequestDTO(null, 1, 1, "INV-001",
            LocalDateTime.now(), 2700.00, null);

        mockMvc.perform(post("/ingress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.details").exists());
    }

    @Test
    @DisplayName("Debería retornar Not Found cuando el ingreso no existe")
    void shouldReturnNotFound_whenIngressDoesNotExist() throws Exception {
        when(ingressService.readById(999)).thenThrow(new ModelNotFoundException("Ingress not found"));

        mockMvc.perform(get("/ingress/{idIngress}", 999)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Debería retornar lista vacía cuando no hay ingresos")
    void shouldReturnEmptyList_whenNoIngressesExist() throws Exception {
        when(ingressService.readAll()).thenReturn(List.of());

        mockMvc.perform(get("/ingress")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));
    }
}

