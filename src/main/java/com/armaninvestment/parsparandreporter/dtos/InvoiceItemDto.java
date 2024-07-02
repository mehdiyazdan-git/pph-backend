package com.armaninvestment.parsparandreporter.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceItemDto implements Serializable {
    private Long id;
    private Long productId;
    private Integer quantity;
    private Long unitPrice;
    private Long invoiceId;
    private Long warehouseReceiptId;

    public boolean receiptIdIsEqual(Long otherReceiptId) {
        return Objects.equals(this.getWarehouseReceiptId(), otherReceiptId);
    }

    @Override
    public String toString() {
        return "InvoiceItemDto{" +
                "id=" + id +
                ", productId=" + productId +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", invoiceId=" + invoiceId +
                ", warehouseReceiptId=" + warehouseReceiptId +
                '}';
    }
}