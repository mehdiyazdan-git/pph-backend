package com.armaninvestment.parsparandreporter.dtos;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.util.Objects;

@NoArgsConstructor
@Getter
@Setter
@Entity
public class CompanyReportDTO {
    @Id
    private Long id;
    private String name;
    private Boolean bigCustomer;
    private Long quantity;
    private Long avg_unit_price;
    private Long amount;
    private Long cumulative_quantity;


    public CompanyReportDTO(Long id, String name, Boolean bigCustomer, Long quantity, Long avg_unit_price, Long amount, Long cumulative_quantity) {
        this.id = id;
        this.name = name;
        this.bigCustomer = bigCustomer;
        this.quantity = quantity;
        this.avg_unit_price = avg_unit_price;
        this.amount = amount;
        this.cumulative_quantity = cumulative_quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        CompanyReportDTO that = (CompanyReportDTO) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

