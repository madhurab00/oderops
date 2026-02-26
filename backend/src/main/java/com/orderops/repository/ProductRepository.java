package com.orderops.repository;

import com.orderops.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    boolean existsBySku(String sku);
    boolean existsBySkuAndIdNot(String sku, UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Product> findByIdIn(List<UUID> ids);
}