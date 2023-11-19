package com.armaninvestment.parsparandreporter.dtos;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "\"MY_TABLE\"")
public class MyTable {
    @Id
    @NotNull
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "receiptno")
    private Long receiptno;

    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;

    @Column(name = "customer_code", length = Integer.MAX_VALUE)
    private String customerCode;

    @Column(name = "customer_name", length = Integer.MAX_VALUE)
    private String customerName;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "total_quantity")
    private Long totalQuantity;

    @Column(name = "item_count")
    private Long itemCount;

}