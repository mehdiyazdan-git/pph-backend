package com.armaninvestment.parsparandreporter.mappers;

import com.armaninvestment.parsparandreporter.dtos.WarehouseReceiptItemDto;
import com.armaninvestment.parsparandreporter.entities.Product;
import com.armaninvestment.parsparandreporter.entities.WarehouseReceiptItem;
import com.armaninvestment.parsparandreporter.repositories.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WarehouseReceiptItemMapperImpl implements WarehouseReceiptItemMapper {
    private final ProductRepository productRepository;

    @Autowired
    public WarehouseReceiptItemMapperImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public WarehouseReceiptItem toEntity(WarehouseReceiptItemDto warehouseReceiptItemDto) {
        if (warehouseReceiptItemDto == null) {
            return null;
        } else {
            WarehouseReceiptItem warehouseReceiptItem = new WarehouseReceiptItem();
            warehouseReceiptItem.setProduct(this.toProductEntity(warehouseReceiptItemDto));
            warehouseReceiptItem.setId(warehouseReceiptItemDto.getId());
            warehouseReceiptItem.setQuantity(warehouseReceiptItemDto.getQuantity());
            warehouseReceiptItem.setUnitPrice(warehouseReceiptItemDto.getUnitPrice());
            return warehouseReceiptItem;
        }
    }

    public WarehouseReceiptItemDto toDto(WarehouseReceiptItem warehouseReceiptItem) {
        if (warehouseReceiptItem == null) {
            return null;
        } else {
            WarehouseReceiptItemDto warehouseReceiptItemDto = new WarehouseReceiptItemDto();
            warehouseReceiptItemDto.setProductId(this.warehouseReceiptItemProductId(warehouseReceiptItem));
            warehouseReceiptItemDto.setId(warehouseReceiptItem.getId());
            warehouseReceiptItemDto.setQuantity(warehouseReceiptItem.getQuantity());
            warehouseReceiptItemDto.setUnitPrice(warehouseReceiptItem.getUnitPrice());
            return warehouseReceiptItemDto;
        }
    }

    public WarehouseReceiptItem partialUpdate(WarehouseReceiptItemDto warehouseReceiptItemDto, WarehouseReceiptItem warehouseReceiptItem) {
        if (warehouseReceiptItemDto == null) {
            return null;
        } else {
            if (warehouseReceiptItem.getProduct() == null) {
                warehouseReceiptItem.setProduct(new Product());
            }

            if (warehouseReceiptItemDto.getProductId() != null) {
                warehouseReceiptItem.setProduct(toProductEntity(warehouseReceiptItemDto));
            }

            if (warehouseReceiptItemDto.getQuantity() != null) {
                warehouseReceiptItem.setQuantity(warehouseReceiptItemDto.getQuantity());
            }

            if (warehouseReceiptItemDto.getUnitPrice() != null) {
                warehouseReceiptItem.setUnitPrice(warehouseReceiptItemDto.getUnitPrice());
            }

            return warehouseReceiptItem;
        }
    }

    protected Product toProductEntity(WarehouseReceiptItemDto warehouseReceiptItemDto) {
        if (warehouseReceiptItemDto == null) {
            return null;
        } else {
            Long productId = warehouseReceiptItemDto.getProductId();
            return productRepository.findById(productId).orElseThrow(() -> new EntityNotFoundException("محصول با شناسه " + productId + " یافت نشد."));
        }
    }

    private Long warehouseReceiptItemProductId(WarehouseReceiptItem warehouseReceiptItem) {
        if (warehouseReceiptItem == null) {
            return null;
        } else {
            Product product = warehouseReceiptItem.getProduct();
            if (product == null) {
                return null;
            } else {
                Long id = product.getId();
                return id == null ? null : id;
            }
        }
    }
}
