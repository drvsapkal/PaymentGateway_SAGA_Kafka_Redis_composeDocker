package com.app.paymentsystem.inventory.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.paymentsystem.inventory.entity.Inventory;
import com.app.paymentsystem.inventory.repository.InventoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository repo;

    public boolean reserve(Long productId, int qty) {
    	 int updated = repo.reserveStock(productId, qty);
         return updated > 0;
    }

	public Object addStock(List<Inventory> inventories) {
		
		return repo.saveAll(inventories);
	}
}

