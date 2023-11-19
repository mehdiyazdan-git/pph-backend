package com.armaninvestment.parsparandreporter.mappers;

import com.armaninvestment.parsparandreporter.dtos.PaymentDto;
import com.armaninvestment.parsparandreporter.entities.Payment;

public interface PaymentMapper {
    Payment toEntity(PaymentDto paymentDto);

    PaymentDto toDto(Payment payment);

    Payment partialUpdate(PaymentDto paymentDto, Payment payment);
}