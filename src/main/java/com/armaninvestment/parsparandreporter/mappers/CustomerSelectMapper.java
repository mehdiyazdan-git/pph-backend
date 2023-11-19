package com.armaninvestment.parsparandreporter.mappers;


import com.armaninvestment.parsparandreporter.dtos.CustomerSelectDto;
import com.armaninvestment.parsparandreporter.entities.Customer;


public interface CustomerSelectMapper {

    Customer toEntity(CustomerSelectDto customerSelectDto);

    CustomerSelectDto toDto(Customer customer);

}
