package com.armaninvestment.parsparandreporter.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.time.LocalDate;
import java.util.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "contracts")
public class Contract {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contract_number")
    private String contractNumber;

    @Column(name = "contract_description")
    private String contractDescription;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "advance_payment")
    private Long advancePayment;

    @Column(name = "performance_bond")
    private Long performanceBond;

    @Column(name = "insurance_deposit")
    private Long insuranceDeposit;

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL)
    private List<Addendum> addendums = new ArrayList<>();

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "customer_id", foreignKey = @ForeignKey(name = "fk_contract__customer"))
    private Customer customer;

    @OneToMany(mappedBy = "contract", fetch = FetchType.EAGER, cascade = {CascadeType.MERGE})
    private Set<Invoice> invoices = new LinkedHashSet<>();

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<ContractItem> contractItems = new LinkedHashSet<>();

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH})
    @JoinColumn(name = "year_id")
    private Year year;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Contract contract = (Contract) o;
        return id != null && Objects.equals(id, contract.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
//    // جمع بشکه های تعهد شده در قرارداد و الحاقیه ها
//    public Long getTotalBarrels() {
//        int total = 0;
//        if (addendums != null) {
//            for (Addendum addendum : addendums) {
//                total += addendum.getQuantity();
//            }
//        }
//        return total + quantity;
//    }
//
//    //جمع قیمت قرارداد و الحاقیه ها
//    public Long getTotalAmount() {
//        long totalAmount = 0L;
//        if (quantity != null && unitPrice != null) {
//            totalAmount = quantity * unitPrice;
//        }
//        if (addendums != null) {
//            for (Addendum addendum : addendums) {
//                if (addendum.getQuantity() != null && addendum.getUnitPrice() != null) {
//                    totalAmount += addendum.getQuantity() * addendum.getUnitPrice();
//                }
//            }
//        }
//        return totalAmount;
//    }
