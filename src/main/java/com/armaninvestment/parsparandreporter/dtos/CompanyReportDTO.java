package com.armaninvestment.parsparandreporter.dtos;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
public class CompanyReportDTO {
    @Id
    private Long id;
    private String customerName;
    private Long totalAmount;
    private Long totalQuantity;
    private Long cumulativeTotalQuantity;
    private Long cumulativeTotalAmount;
    private Long avgUnitPrice;

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

