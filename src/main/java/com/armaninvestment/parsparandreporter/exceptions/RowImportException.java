package com.armaninvestment.parsparandreporter.exceptions;

public class RowImportException extends RuntimeException {
    public RowImportException(int rowNum, String message) {
        super("خطا در ردیف " + rowNum + ": " + message);
    }
}

