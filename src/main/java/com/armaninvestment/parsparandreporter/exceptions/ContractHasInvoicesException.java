package com.armaninvestment.parsparandreporter.exceptions;

public class ContractHasInvoicesException extends RuntimeException {
    public ContractHasInvoicesException() {
        super("Contract has associated invoices and cannot be deleted.");
    }
}
