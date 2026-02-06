package com.alonso.salesapp.service.impl;

import com.alonso.salesapp.dto.ingress.IngressRequestDTO;
import com.alonso.salesapp.dto.ingress.IngressResponseDTO;
import com.alonso.salesapp.exception.ModelNotFoundException;
import com.alonso.salesapp.mapper.IngressMapper;
import com.alonso.salesapp.model.*;
import com.alonso.salesapp.repository.*;
import com.alonso.salesapp.service.IngressService;
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
public class IngressServiceImpl implements IngressService {

    private final IngressRepo repo;
    private final IngressMapper mapper;
    private final ProviderRepo providerRepo;
    private final UserRepo userRepo;
    private final ProductRepo productRepo;

    @Override
    public List<IngressResponseDTO> readAll() {
        List<Ingress> ingresses = repo.findAll();
        return mapper.toResponseDTOList(ingresses);
    }

    @Override
    public IngressResponseDTO readById(Integer idIngress) {
        Ingress ingress = repo.findById(idIngress)
                .orElseThrow(() -> new ModelNotFoundException("Sale not found with id: " + idIngress));
        return mapper.toResponseDTO(ingress);
    }

    @Override
    @Transactional
    public IngressResponseDTO create(IngressRequestDTO dto) {
        log.info("Creating ingress: {}", dto);

        Ingress ingress = mapper.toEntity(dto);

        // Validamos null (por si el mapper falló) y isEmpty (lista vacía)
        if (ingress.getDetails() == null || ingress.getDetails().isEmpty()) {
            // Puedes usar ModelNotFoundException o, mejor aún, una excepción propia de negocio
            throw new ModelNotFoundException("No se puede registrar un ingreso sin detalles");
        }

        // Cargar Provider completo usando el ID
        Provider provider = providerRepo.findById(dto.idProvider())
                .orElseThrow(() -> new ModelNotFoundException("Provider not found with id: " + dto.idProvider()));
        ingress.setProvider(provider);

        // Cargar User completo usando el ID
        User user = userRepo.findById(dto.idUser())
                .orElseThrow(() -> new ModelNotFoundException("User not found with id: " + dto.idUser()));
        ingress.setUser(user);

        //Asignar fecha si no viene
        if (dto.dateTime() == null) {
            ingress.setDateTime(LocalDateTime.now());
        }

        // Calcular tax si no viene (18%) y Calcular total
        double calculatedSubTotal = ingress.getDetails().stream()
                .mapToDouble(detail -> detail.getQuantity() * detail.getCost())
                .sum();

        double tax = dto.tax() != null && dto.tax() > 0
                ? dto.tax()
                : calculatedSubTotal * 0.18;

        ingress.setTax(tax);
        ingress.setTotal(calculatedSubTotal + tax); // Total sin tax

        //Cargar productos completos para cada detalle
        loadProductsForDetails(ingress);

        //Guardar venta con detalles (CascadeType.ALL)
        Ingress ingressSaved = repo.save(ingress);
        log.info("Ingerss created successfully with id: {}", ingressSaved.getIdIngress());

        return mapper.toResponseDTO(ingressSaved);
    }

    private void loadProductsForDetails(Ingress ingress) {
        ingress.getDetails().forEach(detail -> {
            Product product = productRepo.findByIdLocked(detail.getProduct().getIdProduct())
                    .orElseThrow(() -> new ModelNotFoundException(
                            "Product not found with id: " + detail.getProduct().getIdProduct()));

            // SUMA: Actualizamos el stock en memoria
            int newStock = product.getStock() + detail.getQuantity();
            product.setStock(newStock);

            detail.setProduct(product);
            detail.setIngress(ingress);
        });
    }
}
