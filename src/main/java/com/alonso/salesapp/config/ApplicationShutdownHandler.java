package com.alonso.salesapp.config;

import com.alonso.salesapp.repository.ProductRepo;
import com.alonso.salesapp.service.ICloudinaryService;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

//Clase para el borrado de todas las imágenes subidas a Cloudinary al detener el servicio, evitando así dejar archivos huérfanos en la nube.
@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationShutdownHandler {

    private final ProductRepo productRepo;
    private final ICloudinaryService cloudinaryService;

    @PreDestroy
    public void cleanUpImages() {
        log.info("Eliminando imágenes subidas antes de detener el servicio...");
        productRepo.findAll().forEach(product -> {
            if (product.getImagePublicId() != null) {
                cloudinaryService.delete(product.getImagePublicId());
            }
        });
    }
}