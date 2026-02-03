package com.alonso.salesapp.service.impl;

import com.alonso.salesapp.dto.ClientDTO;
import com.alonso.salesapp.exception.ModelNotFoundException;
import com.alonso.salesapp.mapper.ClientMapper;
import com.alonso.salesapp.model.Client;
import com.alonso.salesapp.repository.ClientRepo;
import com.alonso.salesapp.service.IClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements IClientService {

    private final ClientRepo repo;
    private final ClientMapper mapper;

    @Override
    public ClientDTO create(ClientDTO dto) {
        return mapper.toDTO(repo.save(mapper.toEntity(dto)));
    }

    @Override
    public ClientDTO update(Integer id, ClientDTO dto) {
        repo.findById(id).orElseThrow(() -> new ModelNotFoundException("Cliente no encontrado ID: " + id));
        Client entity = mapper.toEntity(dto);
        entity.setIdClient(id);
        return mapper.toDTO(repo.save(entity));
    }

    @Override
    public List<ClientDTO> readAll() {
        return repo.findAll().stream().map(mapper::toDTO).toList();
    }

    @Override
    public ClientDTO readById(Integer id) {
        return repo.findById(id).map(mapper::toDTO)
                .orElseThrow(() -> new ModelNotFoundException("Cliente no encontrado ID: " + id));
    }

    @Override
    public void delete(Integer id) {
        repo.findById(id).orElseThrow(() -> new ModelNotFoundException("Cliente no encontrado ID: " + id));
        repo.deleteById(id);
    }
}