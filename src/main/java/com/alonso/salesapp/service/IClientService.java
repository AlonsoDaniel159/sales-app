package com.alonso.salesapp.service;

import com.alonso.salesapp.dto.ClientDTO;

import java.util.List;

public interface IClientService {
    ClientDTO create(ClientDTO dto);
    ClientDTO update(Integer id, ClientDTO dto);
    List<ClientDTO> readAll();
    ClientDTO readById(Integer id);
    void delete(Integer id);
}
