package com.armaninvestment.parsparandreporter.entities;

import com.armaninvestment.parsparandreporter.enums.SalesType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * DTO for {@link Invoice}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceDtoByQuery implements Serializable {
    private Long id;
    private Long invoiceNumber;
    private LocalDate issuedDate;
    private LocalDate dueDate;
    private Set<InvoiceItemDto> invoiceItems = new LinkedHashSet<>();
    private Long contractId;
    private SalesType salesType;
    private Long customerId;
    private Integer invoiceStatusId;
    private Long yearName;
    private Long advancedPayment;
    private Long insuranceDeposit;
    private Long performanceBound;

    /**
     * DTO for {@link InvoiceItem}
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class InvoiceItemDto implements Serializable {
        private Long id;
        private Long productId;
        private Integer quantity;
        private Long unitPrice;
        private Long warehouseReceiptId;
        private Long invoiceId;
    }
}