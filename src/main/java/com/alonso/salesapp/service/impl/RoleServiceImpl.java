package com.alonso.salesapp.service.impl;

import com.alonso.salesapp.dto.RoleDTO;
import com.alonso.salesapp.exception.ModelNotFoundException;
import com.alonso.salesapp.mapper.RoleMapper;
import com.alonso.salesapp.model.Role;
import com.alonso.salesapp.repository.RoleRepo;
import com.alonso.salesapp.service.IRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements IRoleService {

    private final RoleRepo repo;
    private final RoleMapper mapper;

    @Override
    public RoleDTO create(RoleDTO dto) {
        Role role = mapper.toEntity(dto);
        Role savedRole = repo.save(role);
        return mapper.toDTO(savedRole);
    }

    @Override
    public RoleDTO update(Integer id, RoleDTO dto) {
        repo.findById(id)
                .orElseThrow(() -> new ModelNotFoundException("Role not found with id: " + id));
        Role role = mapper.toEntity(dto);
        role.setIdRole(id);
        return mapper.toDTO(repo.save(role));
    }

    @Override
    public List<RoleDTO> readAll() {
        return repo.findAll().stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public RoleDTO readById(Integer id) {
        Role role = repo.findById(id)
                .orElseThrow(() -> new ModelNotFoundException("Role not found with id: " + id));
        return mapper.toDTO(role);
    }

    @Override
    public void delete(Integer id) {
        Role role = repo.findById(id)
                .orElseThrow(() -> new ModelNotFoundException("Role not found with id: " + id));
        role.setEnabled(false);
        repo.save(role);
    }
}
