package com.armaninvestment.parsparandreporter.entities;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;

import java.time.LocalDate;

@EntityView(Payment.class)
public interface PaymentView {
    @IdMapping
    Long getId();

    String getDescription();

    LocalDate getDate();

    Long getAmount();

    Long getCustomerId();

    @Mapping("customer.name")
    String getCustomerName();

    @Mapping("customer.customerCode")
    String getCustomerCustomerCode();
}