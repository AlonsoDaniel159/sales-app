package com.alonso.salesapp.service;

import com.alonso.salesapp.dto.role.RoleDTO;

import java.util.List;

public interface IRoleService {
    RoleDTO create(RoleDTO dto);
    RoleDTO update(Integer id, RoleDTO dto);
    List<RoleDTO> readAll();
    RoleDTO readById(Integer id);
    void delete(Integer id);
}
