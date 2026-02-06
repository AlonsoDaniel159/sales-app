package com.alonso.salesapp.service;

import com.alonso.salesapp.dto.ingress.IngressRequestDTO;
import com.alonso.salesapp.dto.ingress.IngressResponseDTO;

import java.util.List;

public interface IngressService {

    List<IngressResponseDTO> readAll();

    IngressResponseDTO readById(Integer idIngress);

    IngressResponseDTO create(IngressRequestDTO dto);
}
