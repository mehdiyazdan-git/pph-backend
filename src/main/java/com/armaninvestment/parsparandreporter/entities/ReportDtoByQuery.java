package com.armaninvestment.parsparandreporter.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for {@link Report}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportDtoByQuery implements Serializable {
    private Long id;
    private String explanation;
    private LocalDate date;
    private List<ReportItemDto> reportItems = new ArrayList<>();
    private Long yearId;

    /**
     * DTO for {@link ReportItem}
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReportItemDto implements Serializable {
        private Long id;
        private Long unitPrice;
        private Integer quantity;
        private Long customerId;
        private Long warehouseReceiptId;
    }
}