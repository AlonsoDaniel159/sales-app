package com.alonso.salesapp.service.impl;

import com.alonso.salesapp.dto.client.ClientDTO;
import com.alonso.salesapp.exception.ModelNotFoundException;
import com.alonso.salesapp.mapper.ClientMapper;
import com.alonso.salesapp.model.Client;
import com.alonso.salesapp.repository.ClientRepo;
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
@DisplayName("Client Service Tests")
class ClientServiceImplTest {

    @Mock
    private ClientRepo repo;

    @Mock
    private ClientMapper mapper;

    @InjectMocks
    private ClientServiceImpl clientService;

    private Client client;
    private ClientDTO clientDTO;

    @BeforeEach
    void setUp() {
        client = Client.builder()
                .idClient(1)
                .firstName("John")
                .lastName("Doe")
                .cardId("12345678")
                .phoneNumber("987654321")
                .email("john.doe@example.com")
                .build();

        clientDTO = new ClientDTO(1, "John", "Doe", "12345678", "987654321", "john.doe@example.com", "Address");
    }

    @Nested
    @DisplayName("Crear Cliente")
    class CreateTests {

        @Test
        @DisplayName("Debería crear cliente exitosamente")
        void shouldCreateClient_Successfully() {
            when(mapper.toEntity(clientDTO)).thenReturn(client);
            when(repo.save(client)).thenReturn(client);
            when(mapper.toDTO(client)).thenReturn(clientDTO);

            ClientDTO result = clientService.create(clientDTO);

            assertThat(result).isNotNull();
            assertThat(result.firstName()).isEqualTo("John");
            assertThat(result.lastName()).isEqualTo("Doe");
            verify(mapper).toEntity(clientDTO);
            verify(repo).save(client);
            verify(mapper).toDTO(client);
        }

        @Test
        @DisplayName("Debería retornar DTO con todos los datos del cliente")
        void shouldReturnDTO_WithAllClientData() {
            when(mapper.toEntity(clientDTO)).thenReturn(client);
            when(repo.save(client)).thenReturn(client);
            when(mapper.toDTO(client)).thenReturn(clientDTO);

            ClientDTO result = clientService.create(clientDTO);

            assertThat(result.cardId()).isEqualTo("12345678");
            assertThat(result.phoneNumber()).isEqualTo("987654321");
            assertThat(result.email()).isEqualTo("john.doe@example.com");
        }
    }

    @Nested
    @DisplayName("Actualizar Cliente")
    class UpdateTests {

        @Test
        @DisplayName("Debería actualizar cliente exitosamente")
        void shouldUpdateClient_Successfully() {
            ClientDTO updateDTO = new ClientDTO(null, "Jane", "Smith", "87654321", "123456789", "jane@example.com", "Address");
            Client updatedClient = Client.builder()
                    .idClient(1)
                    .firstName("Jane")
                    .lastName("Smith")
                    .build();
            ClientDTO responseDTO = new ClientDTO(1, "Jane", "Smith", "87654321", "123456789", "jane@example.com", "Address");

            when(repo.findById(1)).thenReturn(Optional.of(client));
            when(mapper.toEntity(updateDTO)).thenReturn(updatedClient);
            when(repo.save(any(Client.class))).thenReturn(updatedClient);
            when(mapper.toDTO(updatedClient)).thenReturn(responseDTO);

            ClientDTO result = clientService.update(1, updateDTO);

            assertThat(result).isNotNull();
            assertThat(result.firstName()).isEqualTo("Jane");
            verify(repo).findById(1);
        }

        @Test
        @DisplayName("Debería lanzar exception cuando cliente no existe")
        void shouldThrowException_WhenClientNotFound() {
            ClientDTO updateDTO = new ClientDTO(null, "Jane", "Smith", "87654321", "123456789", "jane@example.com", "Address");

            when(repo.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> clientService.update(999, updateDTO))
                    .isInstanceOf(ModelNotFoundException.class)
                    .hasMessageContaining("Cliente no encontrado ID: 999");

            verify(repo).findById(999);
            verifyNoMoreInteractions(mapper, repo);
        }

        @Test
        @DisplayName("Debería asignar el ID correcto al actualizar")
        void shouldAssignCorrectId_WhenUpdating() {
            ClientDTO updateDTO = new ClientDTO(null, "Jane", "Smith", "87654321", "123456789", "jane@example.com", "Address");

            when(repo.findById(1)).thenReturn(Optional.of(client));
            when(mapper.toEntity(updateDTO)).thenReturn(client);
            when(repo.save(any(Client.class))).thenReturn(client);
            when(mapper.toDTO(client)).thenReturn(clientDTO);

            clientService.update(1, updateDTO);

            ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
            verify(repo).save(captor.capture());
            assertThat(captor.getValue().getIdClient()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Consultar Clientes")
    class ReadTests {

        @Test
        @DisplayName("Debería retornar todos los clientes")
        void shouldReturnAllClients() {
            Client client2 = Client.builder()
                    .idClient(2)
                    .firstName("Jane")
                    .lastName("Smith")
                    .build();
            ClientDTO clientDTO2 = new ClientDTO(2, "Jane", "Smith", "11111111", "999999999", "jane@example.com", "789 Other Ave");

            when(repo.findAll()).thenReturn(List.of(client, client2));
            when(mapper.toDTO(client)).thenReturn(clientDTO);
            when(mapper.toDTO(client2)).thenReturn(clientDTO2);

            List<ClientDTO> result = clientService.readAll();

            assertThat(result).hasSize(2);
            assertThat(result).extracting(ClientDTO::firstName)
                    .containsExactly("John", "Jane");
            verify(repo).findAll();
        }

        @Test
        @DisplayName("Debería retornar lista vacía cuando no hay clientes")
        void shouldReturnEmptyList_WhenNoClientsExist() {
            when(repo.findAll()).thenReturn(List.of());

            List<ClientDTO> result = clientService.readAll();

            assertThat(result).isEmpty();
            verify(repo).findAll();
        }

        @Test
        @DisplayName("Debería retornar cliente por ID")
        void shouldReturnClient_ById() {
            when(repo.findById(1)).thenReturn(Optional.of(client));
            when(mapper.toDTO(client)).thenReturn(clientDTO);

            ClientDTO result = clientService.readById(1);

            assertThat(result).isNotNull();
            assertThat(result.idClient()).isEqualTo(1);
            assertThat(result.firstName()).isEqualTo("John");
            verify(repo).findById(1);
        }

        @Test
        @DisplayName("Debería lanzar exception cuando cliente no existe")
        void shouldThrowException_WhenClientNotFoundById() {
            when(repo.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> clientService.readById(999))
                    .isInstanceOf(ModelNotFoundException.class)
                    .hasMessageContaining("Cliente no encontrado ID: 999");

            verify(repo).findById(999);
        }
    }

    @Nested
    @DisplayName("Eliminar Cliente")
    class DeleteTests {

        @Test
        @DisplayName("Debería eliminar cliente exitosamente")
        void shouldDeleteClient_Successfully() {
            when(repo.findById(1)).thenReturn(Optional.of(client));

            clientService.delete(1);

            verify(repo).findById(1);
            verify(repo).deleteById(1);
        }

        @Test
        @DisplayName("Debería lanzar exception cuando cliente no existe")
        void shouldThrowException_WhenClientNotFound() {
            when(repo.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> clientService.delete(999))
                    .isInstanceOf(ModelNotFoundException.class)
                    .hasMessageContaining("Cliente no encontrado ID: 999");

            verify(repo).findById(999);
            verify(repo, never()).deleteById(any());
        }

        @Test
        @DisplayName("Debería realizar eliminación física")
        void shouldPerformPhysicalDelete() {
            when(repo.findById(1)).thenReturn(Optional.of(client));

            clientService.delete(1);

            verify(repo).deleteById(1);
            verify(repo, never()).save(any());
        }
    }
}
