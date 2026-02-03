package com.alonso.salesapp.repository;

import com.alonso.salesapp.model.Sale;
import jakarta.annotation.Nonnull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SaleRepo extends JpaRepository<Sale, Integer> {

    @EntityGraph(attributePaths = {"details", "client", "user", "details.product"})
    @Nonnull
    List<Sale> findAll();
}
