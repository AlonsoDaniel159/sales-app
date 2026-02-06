package com.alonso.salesapp.service;

import com.alonso.salesapp.dto.provider.ProviderDTO;

import java.util.List;

public interface IProviderService {
    ProviderDTO create(ProviderDTO dto);
    ProviderDTO update(Integer id, ProviderDTO dto);
    List<ProviderDTO> readAll();
    ProviderDTO readById(Integer id);
    void delete(Integer id);
}
