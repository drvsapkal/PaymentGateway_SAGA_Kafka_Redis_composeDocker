package com.app.paymentsystem.order.entity;

import com.app.paymentsystem.order.domain.OrderStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "orders", uniqueConstraints = {
						@UniqueConstraint(columnNames ="transaction_id"),
						@UniqueConstraint(columnNames ="idempotency_key")
						})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
   
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

	@Column(name = "transaction_id", nullable = false, unique = true)
    private String transactionId;   // saga correlation id
	
	@Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;
    
    private Long customerId;
    private Integer amount;
    
    @Enumerated(EnumType.STRING)
    private OrderStatus status;// CREATED, PAYMENT_PENDING, COMPLETED, FAILED
	
}
