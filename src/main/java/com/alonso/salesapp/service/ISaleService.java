package com.alonso.salesapp.service;

import com.alonso.salesapp.dto.sale.SaleDTO;
import com.alonso.salesapp.dto.sale.SaleResponseDTO;

import java.util.List;

public interface ISaleService {

    SaleResponseDTO create(SaleDTO dto);

    SaleResponseDTO readById(Integer idSale);

    List<SaleResponseDTO> readAll();
}