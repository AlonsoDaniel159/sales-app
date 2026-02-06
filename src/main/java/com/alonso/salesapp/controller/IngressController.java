package com.alonso.salesapp.controller;

import com.alonso.salesapp.dto.ingress.IngressRequestDTO;
import com.alonso.salesapp.dto.ingress.IngressResponseDTO;
import com.alonso.salesapp.service.IngressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ingress")
@RequiredArgsConstructor
public class IngressController {

    private final IngressService service;

    @GetMapping
    public ResponseEntity<List<IngressResponseDTO>> readAll() {
        return ResponseEntity.ok(service.readAll());
    }

    @GetMapping("/{idIngress}")
    public ResponseEntity<IngressResponseDTO> readById(@PathVariable Integer idIngress) {
        return ResponseEntity.ok(service.readById(idIngress));
    }

    @PostMapping
    public ResponseEntity<IngressResponseDTO> create(@Valid @RequestBody IngressRequestDTO ingressRequestDTO) {
        IngressResponseDTO created = service.create(ingressRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}