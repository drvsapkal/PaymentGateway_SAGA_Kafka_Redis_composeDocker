package com.app.paymentsystem.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.app.paymentsystem.inventory.entity.Inventory;

import jakarta.transaction.Transactional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    @Modifying
    @Transactional
    @Query("""
        UPDATE Inventory i
        SET i.quantity = i.quantity - :qty
        WHERE i.productId = :productId
          AND i.quantity >= :qty
    """)
    int reserveStock(@Param("productId") Long productId, @Param("qty") int qty);
    
}
