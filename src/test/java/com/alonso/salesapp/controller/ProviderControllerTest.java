package com.alonso.salesapp.controller;

import com.alonso.salesapp.dto.provider.ProviderDTO;
import com.alonso.salesapp.exception.ModelNotFoundException;
import com.alonso.salesapp.service.IProviderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProviderController.class)
class ProviderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IProviderService providerService;

    @Test
    @DisplayName("Debería retornar todos los proveedores cuando se llama a GET /providers")
    void shouldReturnAllProviders_whenGetAllProvidersIsCalled() throws Exception {
        ProviderDTO provider1 = new ProviderDTO(1, "Tech Supplies Inc", "123 Tech Street", true);
        ProviderDTO provider2 = new ProviderDTO(2, "Office Solutions Ltd", "456 Business Ave", true);
        List<ProviderDTO> providers = List.of(provider1, provider2);

        when(providerService.readAll()).thenReturn(providers);

        mockMvc.perform(get("/providers")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].idProvider").value(1))
                .andExpect(jsonPath("$[0].name").value("Tech Supplies Inc"))
                .andExpect(jsonPath("$[0].address").value("123 Tech Street"))
                .andExpect(jsonPath("$[0].enabled").value(true))
                .andExpect(jsonPath("$[1].idProvider").value(2))
                .andExpect(jsonPath("$[1].name").value("Office Solutions Ltd"));
    }

    @Test
    @DisplayName("Debería retornar un proveedor cuando se proporciona un ID válido")
    void shouldReturnProvider_whenValidIdIsProvided() throws Exception {
        ProviderDTO provider = new ProviderDTO(1, "Tech Supplies Inc", "123 Tech Street", true);

        when(providerService.readById(1)).thenReturn(provider);

        mockMvc.perform(get("/providers/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.idProvider").value(1))
                .andExpect(jsonPath("$.name").value("Tech Supplies Inc"))
                .andExpect(jsonPath("$.address").value("123 Tech Street"))
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    @DisplayName("Debería crear un proveedor cuando se proporcionan datos válidos")
    void shouldCreateProvider_whenValidDataIsProvided() throws Exception {
        ProviderDTO inputDTO = new ProviderDTO(null, "New Provider", "789 New Street", true);
        ProviderDTO createdDTO = new ProviderDTO(1, "New Provider", "789 New Street", true);

        when(providerService.create(any(ProviderDTO.class))).thenReturn(createdDTO);

        mockMvc.perform(post("/providers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.idProvider").value(1))
                .andExpect(jsonPath("$.name").value("New Provider"))
                .andExpect(jsonPath("$.address").value("789 New Street"))
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    @DisplayName("Debería retornar Bad Request cuando el nombre está vacío")
    void shouldReturnBadRequest_whenNameIsEmpty() throws Exception {
        ProviderDTO invalidDTO = new ProviderDTO(null, "", "123 Street", true);

        mockMvc.perform(post("/providers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed with 1 error(s)."));
    }

    @Test
    @DisplayName("Debería retornar Bad Request cuando el nombre es null")
    void shouldReturnBadRequest_whenNameIsNull() throws Exception {
        ProviderDTO invalidDTO = new ProviderDTO(null, null, "123 Street", true);

        mockMvc.perform(post("/providers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    @DisplayName("Debería retornar Bad Request cuando la dirección está vacía")
    void shouldReturnBadRequest_whenAddressIsEmpty() throws Exception {
        ProviderDTO invalidDTO = new ProviderDTO(null, "Provider Name", "", true);

        mockMvc.perform(post("/providers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.address").exists());
    }

    @Test
    @DisplayName("Debería retornar Bad Request cuando la dirección es null")
    void shouldReturnBadRequest_whenAddressIsNull() throws Exception {
        ProviderDTO invalidDTO = new ProviderDTO(null, "Provider Name", null, true);

        mockMvc.perform(post("/providers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.address").exists());
    }

    @Test
    @DisplayName("Debería actualizar un proveedor cuando se proporcionan datos válidos")
    void shouldUpdateProvider_whenValidDataIsProvided() throws Exception {
        ProviderDTO updatedDTO = new ProviderDTO(1, "Updated Provider", "Updated Address", false);

        when(providerService.update(eq(1), any(ProviderDTO.class))).thenReturn(updatedDTO);

        mockMvc.perform(put("/providers/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.idProvider").value(1))
                .andExpect(jsonPath("$.name").value("Updated Provider"))
                .andExpect(jsonPath("$.address").value("Updated Address"))
                .andExpect(jsonPath("$.enabled").value(false));
    }

    @Test
    @DisplayName("Debería eliminar un proveedor cuando el ID existe")
    void shouldDeleteProvider_whenIdExists() throws Exception {
        mockMvc.perform(delete("/providers/{id}", 1))
                .andExpect(status().isNoContent());

        Mockito.verify(providerService, Mockito.times(1)).delete(1);
    }

    @Test
    @DisplayName("Debería retornar Not Found cuando el proveedor no existe")
    void shouldReturnNotFound_whenProviderDoesNotExist() throws Exception {
        when(providerService.readById(999)).thenThrow(new ModelNotFoundException("Provider not found"));

        mockMvc.perform(get("/providers/{id}", 999)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Debería retornar lista vacía cuando no hay proveedores")
    void shouldReturnEmptyList_whenNoProvidersExist() throws Exception {
        when(providerService.readAll()).thenReturn(List.of());

        mockMvc.perform(get("/providers")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Debería crear un proveedor con enabled en null")
    void shouldCreateProvider_whenEnabledIsNull() throws Exception {
        ProviderDTO inputDTO = new ProviderDTO(null, "Provider Name", "123 Street", null);
        ProviderDTO createdDTO = new ProviderDTO(1, "Provider Name", "123 Street", null);

        when(providerService.create(any(ProviderDTO.class))).thenReturn(createdDTO);

        mockMvc.perform(post("/providers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.idProvider").value(1))
                .andExpect(jsonPath("$.name").value("Provider Name"))
                .andExpect(jsonPath("$.address").value("123 Street"));
    }
}
