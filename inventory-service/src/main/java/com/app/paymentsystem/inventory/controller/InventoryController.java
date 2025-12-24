package com.app.paymentsystem.inventory.controller;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.paymentsystem.inventory.entity.Inventory;
import com.app.paymentsystem.inventory.service.InventoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService service;

    @PostMapping("/reserve")
    public boolean reserve(@RequestParam Long productId,
                        @RequestParam int qty) {
        return service.reserve(productId, qty);
     
    }
    
    @PostMapping("/addStock")
    public Object addStock(@RequestBody List<Inventory> inventories)
    {
    	return service.addStock(inventories);
    }
}
