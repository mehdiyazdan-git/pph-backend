package com.armaninvestment.parsparandreporter.exceptions;

public class InvoiceExistByNumberAndIssuedDateException extends RuntimeException {
    public InvoiceExistByNumberAndIssuedDateException(String message) {
        super(message);
    }
}
