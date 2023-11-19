package com.armaninvestment.parsparandreporter.projections;

import java.time.LocalDate;

/**
 * Projection for {@link com.armaninvestment.parsparandreporter.entities.Invoice}
 */
public interface InvoiceInfo {
    Long getId();

    Long getInvoiceNumber();

    LocalDate getIssuedDate();

    LocalDate getDueDate();

    ContractInfo getContract();

    InvoiceStatusInfo getInvoiceStatus();

    /**
     * Projection for {@link com.armaninvestment.parsparandreporter.entities.Contract}
     */
    interface ContractInfo {
        Long getId();

        String getContractNumber();

        String getContractDescription();
    }

    /**
     * Projection for {@link com.armaninvestment.parsparandreporter.entities.InvoiceStatus}
     */
    interface InvoiceStatusInfo {
        Integer getId();

        String getName();
    }
}