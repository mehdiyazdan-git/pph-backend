package com.armaninvestment.parsparandreporter.mappers;

import com.armaninvestment.parsparandreporter.dtos.InvoiceItemDto;
import com.armaninvestment.parsparandreporter.entities.Invoice;
import com.armaninvestment.parsparandreporter.entities.InvoiceItem;
import com.armaninvestment.parsparandreporter.entities.Product;
import com.armaninvestment.parsparandreporter.entities.WarehouseReceipt;
import com.armaninvestment.parsparandreporter.exceptions.WarehouseReceiptAlreadyAssociatedException;
import com.armaninvestment.parsparandreporter.repositories.InvoiceItemRepository;
import com.armaninvestment.parsparandreporter.repositories.ProductRepository;
import com.armaninvestment.parsparandreporter.repositories.WarehouseReceiptRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
public class InvoiceItemMapperImpl implements InvoiceItemMapper {
    private final ProductRepository productRepository;
    private final WarehouseReceiptRepository warehouseReceiptRepository;
    private final InvoiceItemRepository invoiceItemRepository;

    @Autowired
    public InvoiceItemMapperImpl(ProductRepository productRepository,
                                 WarehouseReceiptRepository warehouseReceiptRepository,
                                 InvoiceItemRepository invoiceItemRepository) {
        this.productRepository = productRepository;
        this.warehouseReceiptRepository = warehouseReceiptRepository;
        this.invoiceItemRepository = invoiceItemRepository;
    }

    public static String convertToPersianDigits(Long number) {
        String englishDigits = "0123456789";
        String persianDigits = "۰۱۲۳۴۵۶۷۸۹";

        String numberStr = number.toString();
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < numberStr.length(); i++) {
            char ch = numberStr.charAt(i);
            int index = englishDigits.indexOf(ch);
            if (index >= 0) {
                result.append(persianDigits.charAt(index));
            } else {
                result.append(ch);
            }
        }

        return result.toString();
    }

    private boolean isWareHouseReceiptDuplicate(Long warehouseReceiptId, Long currentInvoiceItemId) {
        Optional<WarehouseReceipt> optionalWarehouseReceipt = warehouseReceiptRepository.findById(warehouseReceiptId);
        return optionalWarehouseReceipt.isPresent()
                && optionalWarehouseReceipt.get().getInvoiceItem() != null
                && !Objects.equals(optionalWarehouseReceipt.get().getInvoiceItem().getId(), currentInvoiceItemId);
    }

    public InvoiceItem toEntity(InvoiceItemDto invoiceItemDto) {
        if (invoiceItemDto == null) {
            return null;
        } else {

            // Create a new InvoiceItem
            InvoiceItem invoiceItem = new InvoiceItem();
            invoiceItem.setInvoice(this.getInvoice(invoiceItemDto.getInvoiceId()));
            WarehouseReceipt warehouseReceipt = this.retrieveWarehouseReceipt(invoiceItemDto);
            if (isWareHouseReceiptDuplicate(invoiceItemDto.getWarehouseReceiptId(), invoiceItemDto.getId())) {
                throw new WarehouseReceiptAlreadyAssociatedException("حواله " + warehouseReceipt.getWarehouseReceiptNumber() + " با فاکتور دیگری در ارتباط می باشد.");
            }
            invoiceItem.setWarehouseReceipt(warehouseReceipt);
            invoiceItem.setProduct(this.toProductEntity(invoiceItemDto.getProductId()));
            invoiceItem.setQuantity(invoiceItemDto.getQuantity());
            invoiceItem.setUnitPrice(invoiceItemDto.getUnitPrice());

            return invoiceItem;
        }
    }


    public InvoiceItemDto toDto(InvoiceItem invoiceItem) {
        if (invoiceItem == null) {
            return null;
        } else {
            InvoiceItemDto invoiceItemDto = new InvoiceItemDto();
            invoiceItemDto.setInvoiceId(this.getInvoiceId(invoiceItem));
            invoiceItemDto.setProductId(this.toProductDto(invoiceItem));
            invoiceItemDto.setId(invoiceItem.getId());
            invoiceItemDto.setQuantity(invoiceItem.getQuantity());
            invoiceItemDto.setUnitPrice(invoiceItem.getUnitPrice());
            invoiceItemDto.setWarehouseReceiptId(this.invoiceItemWarehouseReceiptId(invoiceItem));
            return invoiceItemDto;
        }
    }

    public InvoiceItem partialUpdate(InvoiceItemDto invoiceItemDto, InvoiceItem invoiceItem) {
        if (invoiceItemDto == null) {
            return null;
        } else {
            this.setInvoiceId(invoiceItemDto, invoiceItem.getInvoice());

            if (invoiceItemDto.getProductId() != null) {
                invoiceItem.setProduct(this.toProductEntity(invoiceItemDto.getProductId()));
            }

            if (invoiceItemDto.getId() != null) {
                invoiceItem.setId(invoiceItemDto.getId());
            }

            if (invoiceItemDto.getQuantity() != null) {
                invoiceItem.setQuantity(invoiceItemDto.getQuantity());
            }

            if (invoiceItemDto.getUnitPrice() != null) {
                invoiceItem.setUnitPrice(invoiceItemDto.getUnitPrice());
            }

            if (invoiceItemDto.getWarehouseReceiptId() != null) {
                if (isWareHouseReceiptDuplicate(invoiceItemDto.getWarehouseReceiptId(), invoiceItem.getId())) {
                    throw new WarehouseReceiptAlreadyAssociatedException("حواله " + invoiceItemDto.getWarehouseReceiptId() + " تکراریست.");
                }
                invoiceItem.setWarehouseReceipt(this.retrieveWarehouseReceipt(invoiceItemDto));

            }
            return invoiceItem;
        }
    }

    protected Invoice getInvoice(Long invoiceId) {
        if (invoiceId == null) {
            return null;
        } else {
            Invoice invoice = new Invoice();
            invoice.setId(invoiceId);
            return invoice;
        }
    }

    protected Product toProductEntity(Long productId) {
        if (productId == null) {
            throw new IllegalArgumentException("شناسه محصول نمی تواند خالی باشد.");
        }
        Optional<Product> optionalProduct = productRepository.findById(productId);
        return optionalProduct.orElseThrow(() -> new EntityNotFoundException("محصولی ای با شناسه" + productId + "یافت نشد."));
    }

    protected WarehouseReceipt retrieveWarehouseReceipt(InvoiceItemDto invoiceItemDto) {
        if (invoiceItemDto == null) {
            return null;
        } else {
            Optional<WarehouseReceipt> optionalWarehouseReceipt = warehouseReceiptRepository.findById(invoiceItemDto.getWarehouseReceiptId());
            return optionalWarehouseReceipt.orElseThrow(() -> new EntityNotFoundException("حواله ای با شناسه" + invoiceItemDto.getWarehouseReceiptId() + "یافت نشد."));
        }
    }

    private Long getInvoiceId(InvoiceItem invoiceItem) {
        if (invoiceItem == null) {
            return null;
        } else {
            Invoice invoiceRef = invoiceItem.getInvoice();
            if (invoiceRef == null) {
                return null;
            } else {
                return invoiceRef.getId();
            }
        }
    }

    private Long toProductDto(InvoiceItem invoiceItem) {
        if (invoiceItem == null) {
            return null;
        } else {
            Product product = invoiceItem.getProduct();
            if (product == null) {
                return null;
            } else {
                return product.getId();
            }
        }
    }

    protected void setInvoiceId(InvoiceItemDto invoiceItemDto, Invoice mappingTarget) {
        if (invoiceItemDto != null) {
            if (invoiceItemDto.getInvoiceId() != null) {
                mappingTarget.setId(invoiceItemDto.getInvoiceId());
            }

        }
    }

    private Long invoiceItemWarehouseReceiptId(InvoiceItem invoiceItem) {
        if (invoiceItem == null) {
            return null;
        } else {
            WarehouseReceipt warehouseReceipt = invoiceItem.getWarehouseReceipt();
            if (warehouseReceipt == null) {
                return null;
            } else {
                Long id = warehouseReceipt.getId();
                return id == null ? null : id;
            }
        }
    }

}
