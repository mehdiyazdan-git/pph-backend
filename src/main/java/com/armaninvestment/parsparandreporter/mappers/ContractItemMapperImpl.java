package com.armaninvestment.parsparandreporter.mappers;

import com.armaninvestment.parsparandreporter.dtos.ContractItemDto;
import com.armaninvestment.parsparandreporter.entities.ContractItem;
import com.armaninvestment.parsparandreporter.entities.Product;
import com.armaninvestment.parsparandreporter.repositories.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ContractItemMapperImpl implements ContractItemMapper {
    private final ProductRepository productRepository;

    @Autowired
    public ContractItemMapperImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ContractItem toEntity(ContractItemDto contractItemDto) {
        if (contractItemDto == null) {
            return null;
        } else {
            ContractItem contractItem = new ContractItem();
            contractItem.setProduct(this.toProductEntity(contractItemDto));
            contractItem.setId(contractItemDto.getId());
            contractItem.setUnitPrice(contractItemDto.getUnitPrice());
            contractItem.setQuantity(contractItemDto.getQuantity());
            return contractItem;
        }
    }

    public ContractItemDto toDto(ContractItem contractItem) {
        if (contractItem == null) {
            return null;
        } else {
            ContractItemDto contractItemDto = new ContractItemDto();
            contractItemDto.setProductId(this.getProductId(contractItem));
            contractItemDto.setId(contractItem.getId());
            contractItemDto.setUnitPrice(contractItem.getUnitPrice());
            contractItemDto.setQuantity(contractItem.getQuantity());
            return contractItemDto;
        }
    }

    public ContractItem partialUpdate(ContractItemDto contractItemDto, ContractItem contractItem) {
        if (contractItemDto == null) {
            return null;
        } else {
            if (contractItem.getProduct() != null) {
                contractItem.setProduct(this.toProductEntity(contractItemDto));
            }

            if (contractItemDto.getId() != null) {
                contractItem.setId(contractItemDto.getId());
            }

            if (contractItemDto.getUnitPrice() != null) {
                contractItem.setUnitPrice(contractItemDto.getUnitPrice());
            }

            if (contractItemDto.getQuantity() != null) {
                contractItem.setQuantity(contractItemDto.getQuantity());
            }

            return contractItem;
        }
    }


    protected Product toProductEntity(ContractItemDto contractItemDto) {
        if (contractItemDto == null) {
            return null;
        } else {
            Long productId = contractItemDto.getProductId();
            return productRepository.findById(productId).orElseThrow(() -> new EntityNotFoundException("هیچ محصولی با شناسه " + productId + "یافت نشد."));
        }
    }


    private Long getProductId(ContractItem contractItem) {
        if (contractItem == null) {
            return null;
        } else {
            Product product = contractItem.getProduct();
            if (product == null) {
                return null;
            } else {
                return product.getId();
            }
        }
    }

}
