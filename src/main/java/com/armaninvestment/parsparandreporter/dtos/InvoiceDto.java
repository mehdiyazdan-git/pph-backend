package com.armaninvestment.parsparandreporter.dtos;

import com.armaninvestment.parsparandreporter.enums.SalesType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * DTO for {@link com.armaninvestment.parsparandreporter.entities.Invoice}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceDto implements Serializable {
    private Long id;
    private Long invoiceNumber;
    private LocalDate issuedDate;
    private LocalDate dueDate;
    private Set<InvoiceItemDto> invoiceItems = new LinkedHashSet<>();
    private Long contractId;
    private SalesType salesType;
    private Integer invoiceStatusId;
    private Long customerId;
    private Long yearName;
    private Long advancedPayment;
    private Long performanceBound;
    private Long insuranceDeposit;

    @Override
    public String toString() {
        return "InvoiceDto{" +
                "id=" + id +
                ", invoiceNumber=" + invoiceNumber +
                ", issuedDate=" + issuedDate +
                ", dueDate=" + dueDate +
                ", invoiceItems=" + invoiceItems +
                ", contractId=" + contractId +
                ", salesType=" + salesType +
                ", invoiceStatusId=" + invoiceStatusId +
                ", customerId=" + customerId +
                ", yearName=" + yearName +
                ", advancedPayment=" + advancedPayment +
                ", performanceBound=" + performanceBound +
                ", insuranceDeposit=" + insuranceDeposit +
                '}';
    }
}