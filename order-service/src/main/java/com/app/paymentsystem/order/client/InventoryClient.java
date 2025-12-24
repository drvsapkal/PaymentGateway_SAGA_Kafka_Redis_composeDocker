package com.app.paymentsystem.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "inventory-service")
public interface InventoryClient {

	@PostMapping("/inventory/reserve")
	 public boolean reserve(@RequestParam Long productId,
             @RequestParam int qty);
}
