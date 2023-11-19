package com.armaninvestment.parsparandreporter.mappers.dropdowns;

import com.armaninvestment.parsparandreporter.dtos.dropdowns.ProductDropDownDto;
import com.armaninvestment.parsparandreporter.entities.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductDropDownMapperImpl implements ProductDropDownMapper {
    public ProductDropDownMapperImpl() {
    }

    public ProductDropDownDto toDto(Product product) {
        if (product == null) {
            return null;
        } else {
            ProductDropDownDto productDropDownDto = new ProductDropDownDto();
            productDropDownDto.setId(product.getId());
            productDropDownDto.setProductName(product.getProductName());
            return productDropDownDto;
        }
    }
}