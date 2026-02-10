package com.alonso.salesapp.controller;

import com.alonso.salesapp.dto.role.RoleDTO;
import com.alonso.salesapp.exception.ModelNotFoundException;
import com.alonso.salesapp.service.IRoleService;
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

@WebMvcTest(RoleController.class)
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IRoleService roleService;

    @Test
    @DisplayName("Debería retornar todos los roles cuando se llama a GET /roles")
    void shouldReturnAllRoles_whenGetAllRolesIsCalled() throws Exception {
        RoleDTO role1 = new RoleDTO(1, "ADMIN", true);
        RoleDTO role2 = new RoleDTO(2, "USER", true);
        List<RoleDTO> roles = List.of(role1, role2);

        when(roleService.readAll()).thenReturn(roles);

        mockMvc.perform(get("/roles")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].idRole").value(1))
                .andExpect(jsonPath("$[0].name").value("ADMIN"))
                .andExpect(jsonPath("$[0].enabled").value(true))
                .andExpect(jsonPath("$[1].idRole").value(2))
                .andExpect(jsonPath("$[1].name").value("USER"));
    }

    @Test
    @DisplayName("Debería retornar un rol cuando se proporciona un ID válido")
    void shouldReturnRole_whenValidIdIsProvided() throws Exception {
        RoleDTO role = new RoleDTO(1, "ADMIN", true);

        when(roleService.readById(1)).thenReturn(role);

        mockMvc.perform(get("/roles/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.idRole").value(1))
                .andExpect(jsonPath("$.name").value("ADMIN"))
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    @DisplayName("Debería crear un rol cuando se proporcionan datos válidos")
    void shouldCreateRole_whenValidDataIsProvided() throws Exception {
        RoleDTO inputDTO = new RoleDTO(1, "MANAGER", true);
        RoleDTO createdDTO = new RoleDTO(1, "MANAGER", true);

        when(roleService.create(any(RoleDTO.class))).thenReturn(createdDTO);

        mockMvc.perform(post("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.idRole").value(1))
                .andExpect(jsonPath("$.name").value("MANAGER"))
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    @DisplayName("Debería retornar Bad Request cuando el idRole está vacío")
    void shouldReturnBadRequest_whenIdRoleIsEmpty() throws Exception {
        RoleDTO invalidDTO = new RoleDTO(null, "ADMIN", true);

        mockMvc.perform(post("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.idRole").exists());
    }

    @Test
    @DisplayName("Debería retornar Bad Request cuando el nombre está vacío")
    void shouldReturnBadRequest_whenNameIsEmpty() throws Exception {
        RoleDTO invalidDTO = new RoleDTO(1, "", true);

        mockMvc.perform(post("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    @DisplayName("Debería retornar Bad Request cuando el nombre es null")
    void shouldReturnBadRequest_whenNameIsNull() throws Exception {
        RoleDTO invalidDTO = new RoleDTO(1, null, true);

        mockMvc.perform(post("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    @DisplayName("Debería actualizar un rol cuando se proporcionan datos válidos")
    void shouldUpdateRole_whenValidDataIsProvided() throws Exception {
        RoleDTO updatedDTO = new RoleDTO(1, "ADMIN_UPDATED", false);

        when(roleService.update(eq(1), any(RoleDTO.class))).thenReturn(updatedDTO);

        mockMvc.perform(put("/roles/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.idRole").value(1))
                .andExpect(jsonPath("$.name").value("ADMIN_UPDATED"))
                .andExpect(jsonPath("$.enabled").value(false));
    }

    @Test
    @DisplayName("Debería eliminar un rol cuando el ID existe")
    void shouldDeleteRole_whenIdExists() throws Exception {
        mockMvc.perform(delete("/roles/{id}", 1))
                .andExpect(status().isNoContent());

        Mockito.verify(roleService, Mockito.times(1)).delete(1);
    }

    @Test
    @DisplayName("Debería retornar Not Found cuando el rol no existe")
    void shouldReturnNotFound_whenRoleDoesNotExist() throws Exception {
        when(roleService.readById(999)).thenThrow(new ModelNotFoundException("Role not found"));

        mockMvc.perform(get("/roles/{id}", 999)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Debería retornar lista vacía cuando no hay roles")
    void shouldReturnEmptyList_whenNoRolesExist() throws Exception {
        when(roleService.readAll()).thenReturn(List.of());

        mockMvc.perform(get("/roles")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Debería crear un rol con enabled en null")
    void shouldCreateRole_whenEnabledIsNull() throws Exception {
        RoleDTO inputDTO = new RoleDTO(1, "SUPERVISOR", null);
        RoleDTO createdDTO = new RoleDTO(1, "SUPERVISOR", null);

        when(roleService.create(any(RoleDTO.class))).thenReturn(createdDTO);

        mockMvc.perform(post("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.idRole").value(1))
                .andExpect(jsonPath("$.name").value("SUPERVISOR"));
    }
}

