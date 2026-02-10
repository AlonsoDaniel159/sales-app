package com.alonso.salesapp.service.impl;

import com.alonso.salesapp.dto.provider.ProviderDTO;
import com.alonso.salesapp.exception.ModelNotFoundException;
import com.alonso.salesapp.mapper.ProviderMapper;
import com.alonso.salesapp.model.Provider;
import com.alonso.salesapp.repository.ProviderRepo;
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
@DisplayName("Provider Service Tests")
class ProviderServiceImplTest {

    @Mock
    private ProviderRepo repo;

    @Mock
    private ProviderMapper mapper;

    @InjectMocks
    private ProviderServiceImpl providerService;

    private Provider provider;
    private ProviderDTO providerDTO;

    @BeforeEach
    void setUp() {
        provider = new Provider();
        provider.setIdProvider(1);
        provider.setName("Tech Supplies Inc");
        provider.setAddress("123 Tech Street");
        provider.setEnabled(true);

        providerDTO = new ProviderDTO(1, "Tech Supplies Inc", "123 Tech Street", true);
    }

    @Nested
    @DisplayName("Crear Proveedor")
    class CreateTests {

        @Test
        @DisplayName("Debería crear proveedor exitosamente")
        void shouldCreateProvider_Successfully() {
            when(mapper.toEntity(providerDTO)).thenReturn(provider);
            when(repo.save(provider)).thenReturn(provider);
            when(mapper.toDTO(provider)).thenReturn(providerDTO);

            ProviderDTO result = providerService.create(providerDTO);

            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("Tech Supplies Inc");
            assertThat(result.address()).isEqualTo("123 Tech Street");
            verify(mapper).toEntity(providerDTO);
            verify(repo).save(provider);
            verify(mapper).toDTO(provider);
        }

        @Test
        @DisplayName("Debería retornar DTO con enabled en true")
        void shouldReturnDTO_WithEnabledTrue() {
            when(mapper.toEntity(providerDTO)).thenReturn(provider);
            when(repo.save(provider)).thenReturn(provider);
            when(mapper.toDTO(provider)).thenReturn(providerDTO);

            ProviderDTO result = providerService.create(providerDTO);

            assertThat(result.enabled()).isTrue();
        }
    }

    @Nested
    @DisplayName("Actualizar Proveedor")
    class UpdateTests {

        @Test
        @DisplayName("Debería actualizar proveedor exitosamente")
        void shouldUpdateProvider_Successfully() {
            ProviderDTO updateDTO = new ProviderDTO(null, "Updated Provider", "456 New Street", true);
            Provider updatedProvider = new Provider();
            updatedProvider.setIdProvider(1);
            updatedProvider.setName("Updated Provider");
            updatedProvider.setAddress("456 New Street");
            updatedProvider.setEnabled(true);
            ProviderDTO responseDTO = new ProviderDTO(1, "Updated Provider", "456 New Street", true);

            when(repo.findById(1)).thenReturn(Optional.of(provider));
            when(mapper.toEntity(updateDTO)).thenReturn(updatedProvider);
            when(repo.save(any(Provider.class))).thenReturn(updatedProvider);
            when(mapper.toDTO(updatedProvider)).thenReturn(responseDTO);

            ProviderDTO result = providerService.update(1, updateDTO);

            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("Updated Provider");
            assertThat(result.address()).isEqualTo("456 New Street");
            verify(repo).findById(1);
        }

        @Test
        @DisplayName("Debería lanzar exception cuando proveedor no existe")
        void shouldThrowException_WhenProviderNotFound() {
            ProviderDTO updateDTO = new ProviderDTO(null, "Updated Provider", "456 New Street", true);

            when(repo.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> providerService.update(999, updateDTO))
                    .isInstanceOf(ModelNotFoundException.class)
                    .hasMessageContaining("Proveedor no encontrado ID: 999");

            verify(repo).findById(999);
            verifyNoMoreInteractions(mapper, repo);
        }

        @Test
        @DisplayName("Debería asignar el ID correcto al proveedor")
        void shouldAssignCorrectId_ToProvider() {
            ProviderDTO updateDTO = new ProviderDTO(null, "Updated", "Address", true);

            when(repo.findById(1)).thenReturn(Optional.of(provider));
            when(mapper.toEntity(updateDTO)).thenReturn(provider);
            when(repo.save(any(Provider.class))).thenReturn(provider);
            when(mapper.toDTO(provider)).thenReturn(providerDTO);

            providerService.update(1, updateDTO);

            ArgumentCaptor<Provider> captor = ArgumentCaptor.forClass(Provider.class);
            verify(repo).save(captor.capture());
            assertThat(captor.getValue().getIdProvider()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Consultar Proveedores")
    class ReadTests {

        @Test
        @DisplayName("Debería retornar todos los proveedores")
        void shouldReturnAllProviders() {
            Provider provider2 = new Provider();
            provider2.setIdProvider(2);
            provider2.setName("Office Supplies Co");
            provider2.setAddress("789 Office Ave");
            provider2.setEnabled(true);
            ProviderDTO providerDTO2 = new ProviderDTO(2, "Office Supplies Co", "789 Office Ave", true);

            when(repo.findAll()).thenReturn(List.of(provider, provider2));
            when(mapper.toDTO(provider)).thenReturn(providerDTO);
            when(mapper.toDTO(provider2)).thenReturn(providerDTO2);

            List<ProviderDTO> result = providerService.readAll();

            assertThat(result).hasSize(2);
            assertThat(result).extracting(ProviderDTO::name)
                    .containsExactly("Tech Supplies Inc", "Office Supplies Co");
            verify(repo).findAll();
        }

        @Test
        @DisplayName("Debería retornar lista vacía cuando no hay proveedores")
        void shouldReturnEmptyList_WhenNoProvidersExist() {
            when(repo.findAll()).thenReturn(List.of());

            List<ProviderDTO> result = providerService.readAll();

            assertThat(result).isEmpty();
            verify(repo).findAll();
        }

        @Test
        @DisplayName("Debería retornar proveedor por ID")
        void shouldReturnProvider_ById() {
            when(repo.findById(1)).thenReturn(Optional.of(provider));
            when(mapper.toDTO(provider)).thenReturn(providerDTO);

            ProviderDTO result = providerService.readById(1);

            assertThat(result).isNotNull();
            assertThat(result.idProvider()).isEqualTo(1);
            assertThat(result.name()).isEqualTo("Tech Supplies Inc");
            verify(repo).findById(1);
        }

        @Test
        @DisplayName("Debería lanzar exception cuando proveedor no existe")
        void shouldThrowException_WhenProviderNotFoundById() {
            when(repo.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> providerService.readById(999))
                    .isInstanceOf(ModelNotFoundException.class)
                    .hasMessageContaining("Proveedor no encontrado ID: 999");

            verify(repo).findById(999);
        }
    }

    @Nested
    @DisplayName("Eliminar Proveedor")
    class DeleteTests {

        @Test
        @DisplayName("Debería realizar eliminación lógica")
        void shouldPerformLogicalDelete() {
            when(repo.findById(1)).thenReturn(Optional.of(provider));
            when(repo.save(any(Provider.class))).thenReturn(provider);

            providerService.delete(1);

            ArgumentCaptor<Provider> captor = ArgumentCaptor.forClass(Provider.class);
            verify(repo).save(captor.capture());
        }

        @Test
        @DisplayName("Debería lanzar exception cuando proveedor no existe")
        void shouldThrowException_WhenProviderNotFound() {
            when(repo.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> providerService.delete(999))
                    .isInstanceOf(ModelNotFoundException.class)
                    .hasMessageContaining("Proveedor no encontrado ID: 999");

            verify(repo).findById(999);
            verify(repo, never()).save(any());
        }

        @Test
        @DisplayName("No debería eliminar físicamente del repositorio")
        void shouldNotDeletePhysically() {
            when(repo.findById(1)).thenReturn(Optional.of(provider));
            when(repo.save(any(Provider.class))).thenReturn(provider);

            providerService.delete(1);

            verify(repo, never()).deleteById(any());
            verify(repo, never()).delete(any());
        }

        @Test
        @DisplayName("Debería mantener otros datos del proveedor intactos")
        void shouldKeepOtherProviderData_Intact() {
            when(repo.findById(1)).thenReturn(Optional.of(provider));
            when(repo.save(any(Provider.class))).thenReturn(provider);

            providerService.delete(1);

            ArgumentCaptor<Provider> captor = ArgumentCaptor.forClass(Provider.class);
            verify(repo).save(captor.capture());
            Provider deletedProvider = captor.getValue();
            assertThat(deletedProvider.getName()).isEqualTo("Tech Supplies Inc");
            assertThat(deletedProvider.getAddress()).isEqualTo("123 Tech Street");
        }
    }
}
