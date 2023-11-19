package com.armaninvestment.parsparandreporter.exceptions;

public class WarehouseReceiptNotFoundException extends RuntimeException {
    //    public WarehouseReceiptNotFoundException() {
//        super("حواله یافت نشد");
//    }
    public WarehouseReceiptNotFoundException(String message) {
        super(message);
    }
}
