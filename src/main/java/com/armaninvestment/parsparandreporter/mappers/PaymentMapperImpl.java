package com.armaninvestment.parsparandreporter.mappers;

import com.armaninvestment.parsparandreporter.dtos.PaymentDto;
import com.armaninvestment.parsparandreporter.entities.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapperImpl implements PaymentMapper {
    public PaymentMapperImpl() {
    }

    public Payment toEntity(PaymentDto paymentDto) {
        if (paymentDto == null) {
            return null;
        } else {
            Payment payment = new Payment();
            payment.setId(paymentDto.getId());
            payment.setDescription(paymentDto.getDescription());
            payment.setDate(paymentDto.getDate());
            payment.setAmount(paymentDto.getAmount());
            return payment;
        }
    }

    public PaymentDto toDto(Payment payment) {
        if (payment == null) {
            return null;
        } else {
            PaymentDto paymentDto = new PaymentDto();
            paymentDto.setId(payment.getId());
            paymentDto.setDescription(payment.getDescription());
            paymentDto.setDate(payment.getDate());
            paymentDto.setAmount(payment.getAmount());
            return paymentDto;
        }
    }

    public Payment partialUpdate(PaymentDto paymentDto, Payment payment) {
        if (paymentDto == null) {
            return null;
        } else {
            if (paymentDto.getId() != null) {
                payment.setId(paymentDto.getId());
            }

            if (paymentDto.getDescription() != null) {
                payment.setDescription(paymentDto.getDescription());
            }

            if (paymentDto.getDate() != null) {
                payment.setDate(paymentDto.getDate());
            }

            if (paymentDto.getAmount() != null) {
                payment.setAmount(paymentDto.getAmount());
            }

            return payment;
        }
    }
}
