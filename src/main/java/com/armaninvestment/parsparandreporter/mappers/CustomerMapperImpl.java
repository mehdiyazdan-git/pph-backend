package com.armaninvestment.parsparandreporter.mappers;

import com.armaninvestment.parsparandreporter.dtos.CustomerDto;
import com.armaninvestment.parsparandreporter.dtos.PaymentDto;
import com.armaninvestment.parsparandreporter.entities.Customer;
import com.armaninvestment.parsparandreporter.entities.Payment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CustomerMapperImpl implements CustomerMapper {

    private final PaymentMapper paymentMapper;

    @Autowired
    public CustomerMapperImpl(PaymentMapper paymentMapper) {
        this.paymentMapper = paymentMapper;
    }

    public Customer toEntity(CustomerDto customerDto) {
        if (customerDto == null) {
            return null;
        } else {
            Customer customer = new Customer();
            customer.setId(customerDto.getId());
            customer.setName(customerDto.getName());
            customer.setPhone(customerDto.getPhone());
            customer.setCustomerCode(customerDto.getCustomerCode());
            customer.setEconomicCode(customerDto.getEconomicCode());
            customer.setNationalCode(customerDto.getNationalCode());
            customer.setBigCustomer(customerDto.getBigCustomer());
            customer.setPayments(this.paymentDtoListToPaymentList(customerDto.getPayments()));
            this.linkPayments(customer);
            return customer;
        }
    }

    public CustomerDto toDto(Customer customer) {
        if (customer == null) {
            return null;
        } else {
            CustomerDto customerDto = new CustomerDto();
            customerDto.setId(customer.getId());
            customerDto.setName(customer.getName());
            customerDto.setPhone(customer.getPhone());
            customerDto.setCustomerCode(customer.getCustomerCode());
            customerDto.setEconomicCode(customer.getEconomicCode());
            customerDto.setNationalCode(customer.getNationalCode());
            customerDto.setBigCustomer(customer.getBigCustomer());
            customerDto.setPayments(this.paymentListToPaymentDtoList(customer.getPayments()));
            return customerDto;
        }
    }

    public Customer partialUpdate(CustomerDto customerDto, Customer customer) {
        if (customerDto == null) {
            return null;
        } else {
            if (customerDto.getId() != null) {
                customer.setId(customerDto.getId());
            }

            if (customerDto.getName() != null) {
                customer.setName(customerDto.getName());
            }

            if (customerDto.getPhone() != null) {
                customer.setPhone(customerDto.getPhone());
            }

            if (customerDto.getCustomerCode() != null) {
                customer.setCustomerCode(customerDto.getCustomerCode());
            }

            if (customerDto.getEconomicCode() != null) {
                customer.setEconomicCode(customerDto.getEconomicCode());
            }

            if (customerDto.getNationalCode() != null) {
                customer.setNationalCode(customerDto.getNationalCode());
            }
            customer.setBigCustomer(customerDto.getBigCustomer());

            List<Payment> list;
            if (customer.getPayments() != null) {
                list = this.paymentDtoListToPaymentList(customerDto.getPayments());
                if (list != null) {
                    customer.getPayments().clear();
                    customer.getPayments().addAll(list);
                }
            } else {
                list = this.paymentDtoListToPaymentList(customerDto.getPayments());
                if (list != null) {
                    customer.setPayments(list);
                }
            }

            this.linkPayments(customer);
            return customer;
        }
    }

    protected List<Payment> paymentDtoListToPaymentList(List<PaymentDto> list) {
        if (list == null) {
            return null;
        } else {
            List<Payment> list1 = new ArrayList<>(list.size());

            for (PaymentDto paymentDto : list) {
                list1.add(this.paymentMapper.toEntity(paymentDto));
            }

            return list1;
        }
    }

    protected List<PaymentDto> paymentListToPaymentDtoList(List<Payment> list) {
        if (list == null) {
            return null;
        } else {
            List<PaymentDto> list1 = new ArrayList<>(list.size());

            for (Payment payment : list) {
                list1.add(this.paymentMapper.toDto(payment));
            }
            return list1;
        }
    }
}
