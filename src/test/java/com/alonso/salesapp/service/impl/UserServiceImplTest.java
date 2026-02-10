package com.alonso.salesapp.service.impl;

import com.alonso.salesapp.dto.user.UserDTO;
import com.alonso.salesapp.exception.ModelNotFoundException;
import com.alonso.salesapp.mapper.UserMapper;
import com.alonso.salesapp.model.Role;
import com.alonso.salesapp.model.User;
import com.alonso.salesapp.repository.RoleRepo;
import com.alonso.salesapp.repository.UserRepo;
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
@DisplayName("User Service Tests")
class UserServiceImplTest {

    @Mock
    private UserRepo repo;

    @Mock
    private RoleRepo roleRepo;

    @Mock
    private UserMapper mapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDTO userDTO;
    private Role role;

    @BeforeEach
    void setUp() {
        role = new Role();
        role.setIdRole(1);
        role.setName("ADMIN");
        role.setEnabled(true);

        user = new User();
        user.setIdUser(1);
        user.setUsername("admin");
        user.setPassword("password123");
        user.setRole(role);
        user.setEnabled(true);

        userDTO = new UserDTO(1, 1, "admin", "password123", true);
    }

    @Nested
    @DisplayName("Crear Usuario")
    class CreateTests {

        @Test
        @DisplayName("Debería crear usuario exitosamente")
        void shouldCreateUser_Successfully() {
            when(roleRepo.findById(1)).thenReturn(Optional.of(role));
            when(mapper.toEntity(userDTO)).thenReturn(user);
            when(repo.save(any(User.class))).thenReturn(user);
            when(mapper.toDTO(user)).thenReturn(userDTO);

            UserDTO result = userService.createUser(userDTO);

            assertThat(result).isNotNull();
            assertThat(result.username()).isEqualTo("admin");
            assertThat(result.idRole()).isEqualTo(1);
            verify(roleRepo).findById(1);
            verify(repo).save(any(User.class));
        }

        @Test
        @DisplayName("Debería lanzar exception cuando rol no existe")
        void shouldThrowException_WhenRoleNotFound() {
            when(roleRepo.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.createUser(new UserDTO(null, 999, "user", "pass", true)))
                    .isInstanceOf(ModelNotFoundException.class)
                    .hasMessageContaining("Role not found ID: 999");

            verify(roleRepo).findById(999);
            verifyNoInteractions(repo);
        }

        @Test
        @DisplayName("Debería asignar el rol correcto al usuario")
        void shouldAssignCorrectRole_ToUser() {
            when(roleRepo.findById(1)).thenReturn(Optional.of(role));
            when(mapper.toEntity(userDTO)).thenReturn(user);
            when(repo.save(any(User.class))).thenReturn(user);
            when(mapper.toDTO(user)).thenReturn(userDTO);

            userService.createUser(userDTO);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(repo).save(captor.capture());
            assertThat(captor.getValue().getRole()).isEqualTo(role);
        }

        @Test
        @DisplayName("Debería validar que el usuario tenga todos sus datos")
        void shouldValidateUserHasAllData() {
            when(roleRepo.findById(1)).thenReturn(Optional.of(role));
            when(mapper.toEntity(userDTO)).thenReturn(user);
            when(repo.save(any(User.class))).thenReturn(user);
            when(mapper.toDTO(user)).thenReturn(userDTO);

            UserDTO result = userService.createUser(userDTO);

            assertThat(result.username()).isNotNull();
            assertThat(result.password()).isNotNull();
            assertThat(result.enabled()).isTrue();
        }
    }

    @Nested
    @DisplayName("Actualizar Usuario")
    class UpdateTests {

        @Test
        @DisplayName("Debería actualizar usuario exitosamente")
        void shouldUpdateUser_Successfully() {
            UserDTO updateDTO = new UserDTO(null, 1, "updateduser", "newpass", true);
            User updatedUser = new User();
            updatedUser.setIdUser(1);
            updatedUser.setUsername("updateduser");
            updatedUser.setRole(role);

            when(repo.findById(1)).thenReturn(Optional.of(user));
            when(roleRepo.findById(1)).thenReturn(Optional.of(role));
            when(mapper.toEntity(updateDTO)).thenReturn(updatedUser);
            when(repo.save(any(User.class))).thenReturn(updatedUser);
            when(mapper.toDTO(updatedUser)).thenReturn(new UserDTO(1, 1, "updateduser", "newpass", true));

            UserDTO result = userService.updateUser(1, updateDTO);

            assertThat(result).isNotNull();
            assertThat(result.username()).isEqualTo("updateduser");
            verify(repo).findById(1);
            verify(roleRepo).findById(1);
        }

        @Test
        @DisplayName("Debería lanzar exception cuando usuario no existe")
        void shouldThrowException_WhenUserNotFound() {
            UserDTO updateDTO = new UserDTO(null, 1, "updateduser", "newpass", true);

            when(repo.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateUser(999, updateDTO))
                    .isInstanceOf(ModelNotFoundException.class)
                    .hasMessageContaining("User not found ID: 999");

            verify(repo).findById(999);
            verifyNoInteractions(roleRepo);
        }

        @Test
        @DisplayName("Debería lanzar exception cuando rol no existe")
        void shouldThrowException_WhenRoleNotFoundOnUpdate() {
            UserDTO updateDTO = new UserDTO(null, 999, "updateduser", "newpass", true);

            when(repo.findById(1)).thenReturn(Optional.of(user));
            when(roleRepo.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateUser(1, updateDTO))
                    .isInstanceOf(ModelNotFoundException.class)
                    .hasMessageContaining("Role not found ID: 999");

            verify(repo).findById(1);
            verify(roleRepo).findById(999);
        }

        @Test
        @DisplayName("Debería asignar el ID correcto al usuario")
        void shouldAssignCorrectId_ToUser() {
            UserDTO updateDTO = new UserDTO(null, 1, "updateduser", "newpass", true);

            when(repo.findById(1)).thenReturn(Optional.of(user));
            when(roleRepo.findById(1)).thenReturn(Optional.of(role));
            when(mapper.toEntity(updateDTO)).thenReturn(user);
            when(repo.save(any(User.class))).thenReturn(user);
            when(mapper.toDTO(user)).thenReturn(userDTO);

            userService.updateUser(1, updateDTO);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(repo).save(captor.capture());
            assertThat(captor.getValue().getIdUser()).isEqualTo(1);
        }

        @Test
        @DisplayName("Debería actualizar el rol del usuario")
        void shouldUpdateUserRole() {
            Role newRole = new Role();
            newRole.setIdRole(2);
            newRole.setName("USER");
            UserDTO updateDTO = new UserDTO(null, 2, "updateduser", "newpass", true);

            when(repo.findById(1)).thenReturn(Optional.of(user));
            when(roleRepo.findById(2)).thenReturn(Optional.of(newRole));
            when(mapper.toEntity(updateDTO)).thenReturn(user);
            when(repo.save(any(User.class))).thenReturn(user);
            when(mapper.toDTO(user)).thenReturn(new UserDTO(1, 2, "updateduser", "newpass", true));

            userService.updateUser(1, updateDTO);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(repo).save(captor.capture());
            assertThat(captor.getValue().getRole()).isEqualTo(newRole);
        }
    }

    @Nested
    @DisplayName("Consultar Usuarios")
    class ReadTests {

        @Test
        @DisplayName("Debería retornar todos los usuarios")
        void shouldReturnAllUsers() {
            User user2 = new User();
            user2.setIdUser(2);
            user2.setUsername("user2");
            user2.setRole(role);
            UserDTO userDTO2 = new UserDTO(2, 1, "user2", "pass2", true);

            when(repo.findAll()).thenReturn(List.of(user, user2));
            when(mapper.toDTO(user)).thenReturn(userDTO);
            when(mapper.toDTO(user2)).thenReturn(userDTO2);

            List<UserDTO> result = userService.getAllUsers();

            assertThat(result).hasSize(2);
            assertThat(result).extracting(UserDTO::username)
                    .containsExactly("admin", "user2");
            verify(repo).findAll();
        }

        @Test
        @DisplayName("Debería retornar lista vacía cuando no hay usuarios")
        void shouldReturnEmptyList_WhenNoUsersExist() {
            when(repo.findAll()).thenReturn(List.of());

            List<UserDTO> result = userService.getAllUsers();

            assertThat(result).isEmpty();
            verify(repo).findAll();
        }

        @Test
        @DisplayName("Debería retornar usuario por ID")
        void shouldReturnUser_ById() {
            when(repo.findById(1)).thenReturn(Optional.of(user));
            when(mapper.toDTO(user)).thenReturn(userDTO);

            UserDTO result = userService.getUserById(1);

            assertThat(result).isNotNull();
            assertThat(result.idUser()).isEqualTo(1);
            assertThat(result.username()).isEqualTo("admin");
            verify(repo).findById(1);
        }

        @Test
        @DisplayName("Debería lanzar exception cuando usuario no existe")
        void shouldThrowException_WhenUserNotFoundById() {
            when(repo.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserById(999))
                    .isInstanceOf(ModelNotFoundException.class)
                    .hasMessageContaining("User not found ID: 999");

            verify(repo).findById(999);
        }
    }

    @Nested
    @DisplayName("Eliminar Usuario")
    class DeleteTests {

        @Test
        @DisplayName("Debería realizar eliminación lógica")
        void shouldPerformLogicalDelete() {
            when(repo.findById(1)).thenReturn(Optional.of(user));
            when(repo.save(any(User.class))).thenReturn(user);

            userService.deleteUser(1);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(repo).save(captor.capture());
        }

        @Test
        @DisplayName("Debería lanzar exception cuando usuario no existe")
        void shouldThrowException_WhenUserNotFound() {
            when(repo.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.deleteUser(999))
                    .isInstanceOf(ModelNotFoundException.class)
                    .hasMessageContaining("User not found ID: 999");

            verify(repo).findById(999);
            verify(repo, never()).save(any());
        }

        @Test
        @DisplayName("No debería eliminar físicamente del repositorio")
        void shouldNotDeletePhysically() {
            when(repo.findById(1)).thenReturn(Optional.of(user));
            when(repo.save(any(User.class))).thenReturn(user);

            userService.deleteUser(1);

            verify(repo, never()).deleteById(any());
            verify(repo, never()).delete(any());
        }

        @Test
        @DisplayName("Debería mantener otros datos del usuario intactos")
        void shouldKeepOtherUserData_Intact() {
            when(repo.findById(1)).thenReturn(Optional.of(user));
            when(repo.save(any(User.class))).thenReturn(user);

            userService.deleteUser(1);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(repo).save(captor.capture());
            User deletedUser = captor.getValue();
            assertThat(deletedUser.getUsername()).isEqualTo("admin");
            assertThat(deletedUser.getRole()).isEqualTo(role);
        }
    }
}
