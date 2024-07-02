package com.armaninvestment.parsparandreporter.dtos.list;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PaymentDTO {
    private Long id;
    private String paymentDate;
    private String paymentDescription;
    private String paymentSubject;
    private Long paymentAmount;
    private Long customerId;
}

