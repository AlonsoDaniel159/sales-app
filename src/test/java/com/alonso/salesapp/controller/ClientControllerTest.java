package com.alonso.salesapp.controller;

import com.alonso.salesapp.dto.client.ClientDTO;
import com.alonso.salesapp.exception.ModelNotFoundException;
import com.alonso.salesapp.service.IClientService;
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

@WebMvcTest(ClientController.class)
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IClientService clientService;

    @Test
    @DisplayName("Debería retornar todos los clientes cuando se llama a GET /clients")
    void shouldReturnAllClients_whenGetAllClientsIsCalled() throws Exception {
        ClientDTO client1 = new ClientDTO(1, "John", "Doe", "12345678", "987654321", "john.doe@example.com", "123 Main St");
        ClientDTO client2 = new ClientDTO(2, "Jane", "Smith", "87654321", "123456789", "jane.smith@example.com", "456 Oak Ave");
        List<ClientDTO> clients = List.of(client1, client2);

        when(clientService.readAll()).thenReturn(clients);

        mockMvc.perform(get("/clients")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].idClient").value(1))
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[0].lastName").value("Doe"))
                .andExpect(jsonPath("$[0].email").value("john.doe@example.com"))
                .andExpect(jsonPath("$[1].idClient").value(2))
                .andExpect(jsonPath("$[1].firstName").value("Jane"));
    }

    @Test
    @DisplayName("Debería retornar un cliente cuando se proporciona un ID válido")
    void shouldReturnClient_whenValidIdIsProvided() throws Exception {
        ClientDTO client = new ClientDTO(1, "John", "Doe", "12345678", "987654321", "john.doe@example.com", "123 Main St");

        when(clientService.readById(1)).thenReturn(client);

        mockMvc.perform(get("/clients/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.idClient").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.cardId").value("12345678"))
                .andExpect(jsonPath("$.phoneNumber").value("987654321"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.address").value("123 Main St"));
    }

    @Test
    @DisplayName("Debería crear un cliente cuando se proporcionan datos válidos")
    void shouldCreateClient_whenValidDataIsProvided() throws Exception {
        ClientDTO inputDTO = new ClientDTO(null, "John", "Doe", "12345678", "987654321", "john.doe@example.com", "123 Main St");
        ClientDTO createdDTO = new ClientDTO(1, "John", "Doe", "12345678", "987654321", "john.doe@example.com", "123 Main St");

        when(clientService.create(any(ClientDTO.class))).thenReturn(createdDTO);

        mockMvc.perform(post("/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.idClient").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.cardId").value("12345678"))
                .andExpect(jsonPath("$.phoneNumber").value("987654321"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.address").value("123 Main St"));
    }

    @Test
    @DisplayName("Debería retornar Bad Request cuando el nombre está vacío")
    void shouldReturnBadRequest_whenFirstNameIsEmpty() throws Exception {
        ClientDTO invalidDTO = new ClientDTO(null, "", "Doe", "12345678", "987654321", "john.doe@example.com", "123 Main St");

        mockMvc.perform(post("/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed with 1 error(s)."));
    }

    @Test
    @DisplayName("Debería retornar Bad Request cuando el apellido no cumple con la validación de tamaño")
    void shouldReturnBadRequest_whenLastNameIsInvalid() throws Exception {
        ClientDTO invalidDTO = new ClientDTO(null, "John", "Do", "12345678", "987654321", "john.doe@example.com", "123 Main St");

        mockMvc.perform(post("/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.lastName").exists());
    }

    @Test
    @DisplayName("Debería retornar Bad Request cuando el email tiene formato inválido")
    void shouldReturnBadRequest_whenEmailFormatIsInvalid() throws Exception {
        ClientDTO invalidDTO = new ClientDTO(null, "John", "Doe", "12345678", "987654321", "invalid-email", "123 Main St");

        mockMvc.perform(post("/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").value("Formato de email inválido"));
    }

    @Test
    @DisplayName("Debería retornar Bad Request cuando el cardId no cumple con la validación de tamaño")
    void shouldReturnBadRequest_whenCardIdIsInvalid() throws Exception {
        ClientDTO invalidDTO = new ClientDTO(null, "John", "Doe", "123", "987654321", "john.doe@example.com", "123 Main St");

        mockMvc.perform(post("/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.cardId").exists());
    }

    @Test
    @DisplayName("Debería retornar Bad Request cuando el phoneNumber no cumple con la validación de tamaño")
    void shouldReturnBadRequest_whenPhoneNumberIsInvalid() throws Exception {
        ClientDTO invalidDTO = new ClientDTO(null, "John", "Doe", "12345678", "123", "john.doe@example.com", "123 Main St");

        mockMvc.perform(post("/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.phoneNumber").exists());
    }

    @Test
    @DisplayName("Debería actualizar un cliente cuando se proporcionan datos válidos")
    void shouldUpdateClient_whenValidDataIsProvided() throws Exception {
        ClientDTO updatedDTO = new ClientDTO(1, "John Updated", "Doe Updated", "87654321", "123456789", "john.updated@example.com", "456 New St");

        when(clientService.update(eq(1), any(ClientDTO.class))).thenReturn(updatedDTO);

        mockMvc.perform(put("/clients/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.idClient").value(1))
                .andExpect(jsonPath("$.firstName").value("John Updated"))
                .andExpect(jsonPath("$.lastName").value("Doe Updated"))
                .andExpect(jsonPath("$.cardId").value("87654321"))
                .andExpect(jsonPath("$.phoneNumber").value("123456789"))
                .andExpect(jsonPath("$.email").value("john.updated@example.com"))
                .andExpect(jsonPath("$.address").value("456 New St"));
    }

    @Test
    @DisplayName("Debería eliminar un cliente cuando el ID existe")
    void shouldDeleteClient_whenIdExists() throws Exception {
        mockMvc.perform(delete("/clients/{id}", 1))
                .andExpect(status().isNoContent());

        Mockito.verify(clientService, Mockito.times(1)).delete(1);
    }

    @Test
    @DisplayName("Debería retornar Not Found cuando el cliente no existe")
    void shouldReturnNotFound_whenClientDoesNotExist() throws Exception {
        when(clientService.readById(999)).thenThrow(new ModelNotFoundException("Client not found"));

        mockMvc.perform(get("/clients/{id}", 999)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Debería retornar lista vacía cuando no hay clientes")
    void shouldReturnEmptyList_whenNoClientsExist() throws Exception {
        when(clientService.readAll()).thenReturn(List.of());

        mockMvc.perform(get("/clients")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));
    }
}
