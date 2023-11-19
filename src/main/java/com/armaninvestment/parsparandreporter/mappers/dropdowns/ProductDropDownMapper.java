package com.armaninvestment.parsparandreporter.mappers.dropdowns;

import com.armaninvestment.parsparandreporter.dtos.dropdowns.ProductDropDownDto;
import com.armaninvestment.parsparandreporter.entities.Product;


public interface ProductDropDownMapper {
    ProductDropDownDto toDto(Product product);
}