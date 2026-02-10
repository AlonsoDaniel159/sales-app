package com.alonso.salesapp.service.impl;

import com.alonso.salesapp.dto.role.RoleDTO;
import com.alonso.salesapp.exception.ModelNotFoundException;
import com.alonso.salesapp.mapper.RoleMapper;
import com.alonso.salesapp.model.Role;
import com.alonso.salesapp.repository.RoleRepo;
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
@DisplayName("Role Service Tests")
class RoleServiceImplTest {

    @Mock
    private RoleRepo repo;

    @Mock
    private RoleMapper mapper;

    @InjectMocks
    private RoleServiceImpl roleService;

    private Role role;
    private RoleDTO roleDTO;

    @BeforeEach
    void setUp() {
        role = new Role();
        role.setIdRole(1);
        role.setName("ADMIN");
        role.setEnabled(true);

        roleDTO = new RoleDTO(1, "ADMIN", true);
    }

    @Nested
    @DisplayName("Crear Rol")
    class CreateTests {

        @Test
        @DisplayName("Debería crear rol exitosamente")
        void shouldCreateRole_Successfully() {
            when(mapper.toEntity(roleDTO)).thenReturn(role);
            when(repo.save(role)).thenReturn(role);
            when(mapper.toDTO(role)).thenReturn(roleDTO);

            RoleDTO result = roleService.create(roleDTO);

            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("ADMIN");
            assertThat(result.enabled()).isTrue();
            verify(mapper).toEntity(roleDTO);
            verify(repo).save(role);
            verify(mapper).toDTO(role);
        }

        @Test
        @DisplayName("Debería llamar al mapper correctamente")
        void shouldCallMapper_Correctly() {
            when(mapper.toEntity(roleDTO)).thenReturn(role);
            when(repo.save(role)).thenReturn(role);
            when(mapper.toDTO(role)).thenReturn(roleDTO);

            roleService.create(roleDTO);

            verify(mapper, times(1)).toEntity(roleDTO);
            verify(mapper, times(1)).toDTO(role);
        }
    }

    @Nested
    @DisplayName("Actualizar Rol")
    class UpdateTests {

        @Test
        @DisplayName("Debería actualizar rol exitosamente")
        void shouldUpdateRole_Successfully() {
            RoleDTO updateDTO = new RoleDTO(null, "USER", true);
            Role updatedRole = new Role();
            updatedRole.setIdRole(1);
            updatedRole.setName("USER");
            updatedRole.setEnabled(true);
            RoleDTO responseDTO = new RoleDTO(1, "USER", true);

            when(repo.findById(1)).thenReturn(Optional.of(role));
            when(mapper.toEntity(updateDTO)).thenReturn(updatedRole);
            when(repo.save(any(Role.class))).thenReturn(updatedRole);
            when(mapper.toDTO(updatedRole)).thenReturn(responseDTO);

            RoleDTO result = roleService.update(1, updateDTO);

            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("USER");
            verify(repo).findById(1);
        }

        @Test
        @DisplayName("Debería lanzar exception cuando rol no existe")
        void shouldThrowException_WhenRoleNotFound() {
            RoleDTO updateDTO = new RoleDTO(null, "USER", true);

            when(repo.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> roleService.update(999, updateDTO))
                    .isInstanceOf(ModelNotFoundException.class)
                    .hasMessageContaining("Role not found with id: 999");

            verify(repo).findById(999);
            verifyNoMoreInteractions(mapper, repo);
        }

        @Test
        @DisplayName("Debería asignar el ID correcto al rol")
        void shouldAssignCorrectId_ToRole() {
            RoleDTO updateDTO = new RoleDTO(null, "USER", true);

            when(repo.findById(1)).thenReturn(Optional.of(role));
            when(mapper.toEntity(updateDTO)).thenReturn(role);
            when(repo.save(any(Role.class))).thenReturn(role);
            when(mapper.toDTO(role)).thenReturn(roleDTO);

            roleService.update(1, updateDTO);

            ArgumentCaptor<Role> captor = ArgumentCaptor.forClass(Role.class);
            verify(repo).save(captor.capture());
            assertThat(captor.getValue().getIdRole()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Consultar Roles")
    class ReadTests {

        @Test
        @DisplayName("Debería retornar todos los roles")
        void shouldReturnAllRoles() {
            Role role2 = new Role();
            role2.setIdRole(2);
            role2.setName("USER");
            role2.setEnabled(true);
            RoleDTO roleDTO2 = new RoleDTO(2, "USER", true);

            when(repo.findAll()).thenReturn(List.of(role, role2));
            when(mapper.toDTO(role)).thenReturn(roleDTO);
            when(mapper.toDTO(role2)).thenReturn(roleDTO2);

            List<RoleDTO> result = roleService.readAll();

            assertThat(result).hasSize(2);
            assertThat(result).extracting(RoleDTO::name)
                    .containsExactly("ADMIN", "USER");
            verify(repo).findAll();
        }

        @Test
        @DisplayName("Debería retornar lista vacía cuando no hay roles")
        void shouldReturnEmptyList_WhenNoRolesExist() {
            when(repo.findAll()).thenReturn(List.of());

            List<RoleDTO> result = roleService.readAll();

            assertThat(result).isEmpty();
            verify(repo).findAll();
        }

        @Test
        @DisplayName("Debería retornar rol por ID")
        void shouldReturnRole_ById() {
            when(repo.findById(1)).thenReturn(Optional.of(role));
            when(mapper.toDTO(role)).thenReturn(roleDTO);

            RoleDTO result = roleService.readById(1);

            assertThat(result).isNotNull();
            assertThat(result.idRole()).isEqualTo(1);
            assertThat(result.name()).isEqualTo("ADMIN");
            verify(repo).findById(1);
        }

        @Test
        @DisplayName("Debería lanzar exception cuando rol no existe")
        void shouldThrowException_WhenRoleNotFoundById() {
            when(repo.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> roleService.readById(999))
                    .isInstanceOf(ModelNotFoundException.class)
                    .hasMessageContaining("Role not found with id: 999");

            verify(repo).findById(999);
        }
    }

    @Nested
    @DisplayName("Eliminar Rol")
    class DeleteTests {

        @Test
        @DisplayName("Debería realizar eliminación lógica")
        void shouldPerformLogicalDelete() {
            when(repo.findById(1)).thenReturn(Optional.of(role));
            when(repo.save(any(Role.class))).thenReturn(role);

            roleService.delete(1);

            ArgumentCaptor<Role> captor = ArgumentCaptor.forClass(Role.class);
            verify(repo).save(captor.capture());
        }

        @Test
        @DisplayName("Debería lanzar exception cuando rol no existe")
        void shouldThrowException_WhenRoleNotFound() {
            when(repo.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> roleService.delete(999))
                    .isInstanceOf(ModelNotFoundException.class)
                    .hasMessageContaining("Role not found with id: 999");

            verify(repo).findById(999);
            verify(repo, never()).save(any());
        }

        @Test
        @DisplayName("No debería eliminar físicamente del repositorio")
        void shouldNotDeletePhysically() {
            when(repo.findById(1)).thenReturn(Optional.of(role));
            when(repo.save(any(Role.class))).thenReturn(role);

            roleService.delete(1);

            verify(repo, never()).deleteById(any());
            verify(repo, never()).delete(any());
        }

        @Test
        @DisplayName("Debería mantener otros datos del rol intactos")
        void shouldKeepOtherRoleData_Intact() {
            when(repo.findById(1)).thenReturn(Optional.of(role));
            when(repo.save(any(Role.class))).thenReturn(role);

            roleService.delete(1);

            ArgumentCaptor<Role> captor = ArgumentCaptor.forClass(Role.class);
            verify(repo).save(captor.capture());
            Role deletedRole = captor.getValue();
            assertThat(deletedRole.getName()).isEqualTo("ADMIN");
            assertThat(deletedRole.getIdRole()).isEqualTo(1);
        }
    }
}
