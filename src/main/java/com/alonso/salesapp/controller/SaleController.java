package com.alonso.salesapp.controller;

import com.alonso.salesapp.dto.sale.SaleDTO;
import com.alonso.salesapp.dto.sale.SaleResponseDTO;
import com.alonso.salesapp.service.ISaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sales")
@RequiredArgsConstructor
public class SaleController {

    private final ISaleService service;

    @GetMapping
    public ResponseEntity<List<SaleResponseDTO>> readAll() {
        return ResponseEntity.ok(service.readAll());
    }

    @GetMapping("/{idSale}")
    public ResponseEntity<SaleResponseDTO> readById(@PathVariable Integer idSale) {
        return ResponseEntity.ok(service.readById(idSale));
    }

    @PostMapping
    public ResponseEntity<SaleResponseDTO> create(@Valid @RequestBody SaleDTO saleDTO) {
        SaleResponseDTO created = service.create(saleDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{idSale}")
    public ResponseEntity<Void> deleteById(@PathVariable Integer idSale) {
        service.deleteById(idSale);
        return ResponseEntity.noContent().build();
    }
}