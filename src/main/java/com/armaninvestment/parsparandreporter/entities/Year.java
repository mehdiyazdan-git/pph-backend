package com.armaninvestment.parsparandreporter.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Year {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long name;

    public Year(Long id) {
        this.id = id;
    }

    @ToString.Exclude
    @OneToMany(mappedBy = "year")
    private Set<WarehouseReceipt> warehouseReceipts = new LinkedHashSet<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "year")
    private Set<Report> reports = new LinkedHashSet<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "year")
    private Set<Payment> payments = new LinkedHashSet<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "year")
    private Set<Invoice> invoices = new LinkedHashSet<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "year")
    private Set<Contract> contracts = new LinkedHashSet<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "year")
    private Set<Addendum> addendums = new LinkedHashSet<>();

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Year year = (Year) o;
        return getId() != null && Objects.equals(getId(), year.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
