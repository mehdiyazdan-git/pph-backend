package com.armaninvestment.parsparandreporter.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "warehouse_receipt")
public class WarehouseReceipt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "warehouse_receipt_number")
    private Long warehouseReceiptNumber;

    @Column(name = "warehouse_receipt_date")
    private LocalDate warehouseReceiptDate;

    @Column(name = "warehouse_receipt_description")
    private String warehouseReceiptDescription;

    @OneToMany(mappedBy = "warehouseReceipt", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH}, orphanRemoval = true)
    private List<WarehouseReceiptItem> warehouseReceiptItems = new ArrayList<>();


    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "year_id")
    private Year year;


    @OneToOne(mappedBy = "warehouseReceipt")
    private ReportItem reportItem;

    @OneToOne(mappedBy = "warehouseReceipt")
    private InvoiceItem invoiceItem;

    public WarehouseReceipt(Long id) {
        this.id = id;
    }

    public Long calculateTotalAmount() {
        return warehouseReceiptItems.stream()
                .mapToLong(item -> item.getQuantity() * item.getUnitPrice())
                .sum();
    }


    public Integer calculateTotalQuantity() {
        return warehouseReceiptItems.stream()
                .mapToInt(WarehouseReceiptItem::getQuantity)
                .sum();
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        WarehouseReceipt that = (WarehouseReceipt) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}