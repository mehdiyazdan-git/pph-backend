package com.armaninvestment.parsparandreporter.exceptions;

public class InvoiceExistByNumberAndYearNameException extends RuntimeException {
    public InvoiceExistByNumberAndYearNameException(String message) {
        super(message);
    }
}
