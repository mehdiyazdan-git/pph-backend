package com.armaninvestment.parsparandreporter.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@Table(name = "customer")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "phone")
    private String phone;

    @Column(name = "customer_code")
    private String customerCode;

    @Column(name = "economic_code")
    private String economicCode;

    @Column(name = "national_code")
    private String nationalCode;

    @Column(name = "monthly_report")
    private boolean bigCustomer;


    @OneToMany(mappedBy = "customer")
    private List<ReportItem> reportItems = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Payment> payments = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<Contract> contracts = new LinkedHashSet<>();


    @OneToMany(mappedBy = "customer", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private Set<WarehouseReceipt> warehouseReceipts = new LinkedHashSet<>();

    @OneToMany(mappedBy = "customer", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private Set<Invoice> invoices = new LinkedHashSet<>();

    public boolean getBigCustomer() {
        return bigCustomer;
    }

    public void setBigCustomer(boolean bigCustomer) {
        this.bigCustomer = bigCustomer;
    }

    public Customer(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Customer customer = (Customer) o;
        return getId() != null && Objects.equals(getId(), customer.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}







