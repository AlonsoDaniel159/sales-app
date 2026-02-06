package com.alonso.salesapp.service.impl;

import com.alonso.salesapp.dto.provider.ProviderDTO;
import com.alonso.salesapp.exception.ModelNotFoundException;
import com.alonso.salesapp.mapper.ProviderMapper;
import com.alonso.salesapp.model.Provider;
import com.alonso.salesapp.repository.ProviderRepo;
import com.alonso.salesapp.service.IProviderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProviderServiceImpl implements IProviderService {

    private final ProviderRepo repo;
    private final ProviderMapper mapper;

    @Override
    public ProviderDTO create(ProviderDTO dto) {
        Provider provider = mapper.toEntity(dto);
        return mapper.toDTO(repo.save(provider));
    }

    @Override
    public ProviderDTO update(Integer id, ProviderDTO dto) {
        repo.findById(id).orElseThrow(() -> new ModelNotFoundException("Proveedor no encontrado ID: " + id));
        Provider provider = mapper.toEntity(dto);
        provider.setIdProvider(id);
        return mapper.toDTO(repo.save(provider));
    }

    @Override
    public List<ProviderDTO> readAll() {
        return repo.findAll().stream().map(mapper::toDTO).toList();
    }

    @Override
    public ProviderDTO readById(Integer id) {
        return repo.findById(id)
                .map(mapper::toDTO)
                .orElseThrow(() -> new ModelNotFoundException("Proveedor no encontrado ID: " + id));
    }

    @Override
    public void delete(Integer id) {
        Provider provider = repo.findById(id).orElseThrow(() -> new ModelNotFoundException("Proveedor no encontrado ID: " + id));
        provider.setEnabled(false);
        repo.save(provider);
    }
}
