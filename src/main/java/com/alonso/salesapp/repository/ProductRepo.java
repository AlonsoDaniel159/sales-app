package com.alonso.salesapp.repository;

import com.alonso.salesapp.model.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepo extends JpaRepository<Product, Integer> {

    // Bloquea la fila para escritura. Nadie más puede leerla ni escribirla hasta que termine la transacción.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.idProduct = :id")
    Optional<Product> findByIdLocked(@Param("id") Integer id);
}
