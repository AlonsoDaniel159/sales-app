package com.alonso.salesapp.repository;

import com.alonso.salesapp.model.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProviderRepo extends JpaRepository<Provider, Integer> {
}
