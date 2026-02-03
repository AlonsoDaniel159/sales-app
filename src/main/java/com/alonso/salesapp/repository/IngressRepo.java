package com.alonso.salesapp.repository;

import com.alonso.salesapp.model.Ingress;
import jakarta.annotation.Nonnull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IngressRepo extends JpaRepository<Ingress, Integer> {

    @EntityGraph(attributePaths = {"details", "provider", "user", "details.product"})
    @Nonnull
    List<Ingress> findAll();
}
