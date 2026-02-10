package com.alonso.salesapp.controller;

import com.alonso.salesapp.dto.user.UserDTO;
import com.alonso.salesapp.exception.ModelNotFoundException;
import com.alonso.salesapp.service.IUserService;
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

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IUserService userService;

    @Test
    @DisplayName("Debería retornar todos los usuarios cuando se llama a GET /users")
    void shouldReturnAllUsers_whenGetAllUsersIsCalled() throws Exception {
        UserDTO user1 = new UserDTO(1, 1, "admin", "password123", true);
        UserDTO user2 = new UserDTO(2, 2, "user1", "password456", true);
        List<UserDTO> users = List.of(user1, user2);

        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].idUser").value(1))
                .andExpect(jsonPath("$[0].username").value("admin"))
                .andExpect(jsonPath("$[0].idRole").value(1))
                .andExpect(jsonPath("$[0].enabled").value(true))
                .andExpect(jsonPath("$[1].idUser").value(2))
                .andExpect(jsonPath("$[1].username").value("user1"));
    }

    @Test
    @DisplayName("Debería retornar un usuario cuando se proporciona un ID válido")
    void shouldReturnUser_whenValidIdIsProvided() throws Exception {
        UserDTO user = new UserDTO(1, 1, "admin", "password123", true);

        when(userService.getUserById(1)).thenReturn(user);

        mockMvc.perform(get("/users/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.idUser").value(1))
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.idRole").value(1))
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    @DisplayName("Debería crear un usuario cuando se proporcionan datos válidos")
    void shouldCreateUser_whenValidDataIsProvided() throws Exception {
        UserDTO inputDTO = new UserDTO(null, 1, "newuser", "password123", true);
        UserDTO createdDTO = new UserDTO(1, 1, "newuser", "password123", true);

        when(userService.createUser(any(UserDTO.class))).thenReturn(createdDTO);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.idUser").value(1))
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.idRole").value(1))
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    @DisplayName("Debería retornar Bad Request cuando el idRole es null")
    void shouldReturnBadRequest_whenIdRoleIsNull() throws Exception {
        UserDTO invalidDTO = new UserDTO(null, null, "newuser", "password123", true);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.idRole").exists());
    }

    @Test
    @DisplayName("Debería retornar Bad Request cuando el username está vacío")
    void shouldReturnBadRequest_whenUsernameIsEmpty() throws Exception {
        UserDTO invalidDTO = new UserDTO(null, 1, "", "password123", true);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.username").exists());
    }

    @Test
    @DisplayName("Debería retornar Bad Request cuando el username es null")
    void shouldReturnBadRequest_whenUsernameIsNull() throws Exception {
        UserDTO invalidDTO = new UserDTO(null, 1, null, "password123", true);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.username").exists());
    }

    @Test
    @DisplayName("Debería retornar Bad Request cuando el password está vacío")
    void shouldReturnBadRequest_whenPasswordIsEmpty() throws Exception {
        UserDTO invalidDTO = new UserDTO(null, 1, "newuser", "", true);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    @DisplayName("Debería retornar Bad Request cuando el password es null")
    void shouldReturnBadRequest_whenPasswordIsNull() throws Exception {
        UserDTO invalidDTO = new UserDTO(null, 1, "newuser", null, true);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    @DisplayName("Debería actualizar un usuario cuando se proporcionan datos válidos")
    void shouldUpdateUser_whenValidDataIsProvided() throws Exception {
        UserDTO updatedDTO = new UserDTO(1, 2, "updateduser", "newpassword", false);

        when(userService.updateUser(eq(1), any(UserDTO.class))).thenReturn(updatedDTO);

        mockMvc.perform(put("/users/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.idUser").value(1))
                .andExpect(jsonPath("$.username").value("updateduser"))
                .andExpect(jsonPath("$.idRole").value(2))
                .andExpect(jsonPath("$.enabled").value(false));
    }

    @Test
    @DisplayName("Debería eliminar un usuario cuando el ID existe")
    void shouldDeleteUser_whenIdExists() throws Exception {
        mockMvc.perform(delete("/users/{id}", 1))
                .andExpect(status().isNoContent());

        Mockito.verify(userService, Mockito.times(1)).deleteUser(1);
    }

    @Test
    @DisplayName("Debería retornar Not Found cuando el usuario no existe")
    void shouldReturnNotFound_whenUserDoesNotExist() throws Exception {
        when(userService.getUserById(999)).thenThrow(new ModelNotFoundException("User not found"));

        mockMvc.perform(get("/users/{id}", 999)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Debería retornar lista vacía cuando no hay usuarios")
    void shouldReturnEmptyList_whenNoUsersExist() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of());

        mockMvc.perform(get("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Debería crear un usuario con enabled en null")
    void shouldCreateUser_whenEnabledIsNull() throws Exception {
        UserDTO inputDTO = new UserDTO(null, 1, "newuser", "password123", null);
        UserDTO createdDTO = new UserDTO(1, 1, "newuser", "password123", null);

        when(userService.createUser(any(UserDTO.class))).thenReturn(createdDTO);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.idUser").value(1))
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.idRole").value(1));
    }
}

