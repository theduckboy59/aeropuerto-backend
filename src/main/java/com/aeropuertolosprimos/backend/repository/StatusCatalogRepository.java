package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.StatusCatalog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StatusCatalogRepository extends JpaRepository<StatusCatalog, Integer> {

    Optional<StatusCatalog> findByNameIgnoreCase(String name);

}

