
package com.alonso.salesapp.service.impl;

import com.alonso.salesapp.dto.sale.SaleDTO;
import com.alonso.salesapp.dto.sale.SaleResponseDTO;
import com.alonso.salesapp.exception.ModelNotFoundException;
import com.alonso.salesapp.mapper.SaleMapper;
import com.alonso.salesapp.model.*;
import com.alonso.salesapp.repository.*;
import com.alonso.salesapp.service.ISaleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SaleServiceImpl implements ISaleService {

    private final SaleRepo repo;
    private final SaleMapper mapper;
    private final ClientRepo clientRepo;
    private final UserRepo userRepo;
    private final ProductRepo productRepo;

    @Override
    public List<SaleResponseDTO> readAll() {
        List<Sale> sales = repo.findAll();
        log.info("sales: {}", sales);
        return mapper.toResponseDTOList(sales);
    }

    @Override
    public SaleResponseDTO readById(Integer idSale) {
        Sale sale = repo.findById(idSale)
                .orElseThrow(() -> new ModelNotFoundException("Sale not found with id: " + idSale));
        return mapper.toResponseDTO(sale);
    }

    @Override
    @Transactional
    public SaleResponseDTO create(SaleDTO saleDTO) {
        log.info("Creating sale: {}", saleDTO);

        // 1. Mapear DTO → Entity (MapStruct crea estructura básica)
        Sale sale = mapper.toEntity(saleDTO);

        // Validamos null (por si el mapper falló) y isEmpty (lista vacía)
        if (sale.getDetails() == null || sale.getDetails().isEmpty()) {
            // Puedes usar ModelNotFoundException o, mejor aún, una excepción propia de negocio
            throw new ModelNotFoundException("No se puede registrar una venta sin detalles");
        }

        // 2. Cargar Client completo usando el ID
        Client client = clientRepo.findById(saleDTO.idClient())
                .orElseThrow(() -> new ModelNotFoundException("Client not found with id: " + saleDTO.idClient()));
        sale.setClient(client);

        // 3. Cargar User completo usando el ID
        User user = userRepo.findById(saleDTO.idUser())
                .orElseThrow(() -> new ModelNotFoundException("User not found with id: " + saleDTO.idUser()));
        sale.setUser(user);

        // 4. Cargar productos completos para cada detalle
        sale.getDetails().forEach(detail -> {
            Product product = productRepo.findByIdLocked(detail.getProduct().getIdProduct())
                    .orElseThrow(() -> new ModelNotFoundException(
                            "Product not found with id: " + detail.getProduct().getIdProduct()));

            // 2. VALIDACIÓN: ¿Hay suficiente stock?
            if (product.getStock() < detail.getQuantity()) {
                throw new ModelNotFoundException("Stock insuficiente para el producto: " + product.getName()
                        + ". Stock actual: " + product.getStock());
            }

            // 3. RESTA: Actualizamos el stock en memoria
            int newStock = product.getStock() - detail.getQuantity();
            product.setStock(newStock);

            detail.setProduct(product);
            detail.setSalePrice(product.getPrice());
            detail.setSale(sale);
        });

        // 5. Asignar fecha si no viene
        if (saleDTO.dateTime() == null) {
            sale.setDateTime(LocalDateTime.now());
        }

        // 6. y 7. Calcular tax si no viene (18%) y Calcular total
        double calculatedSubTotal = sale.getDetails().stream()
                .mapToDouble(detail -> (detail.getQuantity() * detail.getSalePrice()) - detail.getDiscount())
                .sum();

        double tax = saleDTO.tax() != null && saleDTO.tax() > 0
                ? saleDTO.tax()
                : calculatedSubTotal * 0.18;

        sale.setTax(tax);
        sale.setTotal(calculatedSubTotal + tax); // Total sin tax

        // 8. Guardar venta con detalles (CascadeType.ALL)
        Sale savedSale = repo.save(sale);

        log.info("Sale created successfully with id: {}", savedSale.getIdSale());

        // 9. Retornar DTO con objetos completos
        return mapper.toResponseDTO(savedSale);
    }
}