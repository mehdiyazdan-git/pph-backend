package com.armaninvestment.parsparandreporter.mappers;

import com.armaninvestment.parsparandreporter.dtos.AdjustmentDto;
import com.armaninvestment.parsparandreporter.entities.Adjustment;
import com.armaninvestment.parsparandreporter.entities.Invoice;
import com.armaninvestment.parsparandreporter.repositories.InvoiceRepository;
import org.springframework.stereotype.Component;

@Component
public class AdjustmentMapperImpl implements AdjustmentMapper {
    private final InvoiceRepository invoiceRepository;

    public AdjustmentMapperImpl(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public Adjustment toEntity(AdjustmentDto adjustmentDto) {
        if (adjustmentDto == null) {
            return null;
        } else {
            Adjustment adjustment = new Adjustment();
            adjustment.setInvoice(this.adjustmentDtoToInvoice(adjustmentDto));
            adjustment.setId(adjustmentDto.getId());
            adjustment.setDescription(adjustmentDto.getDescription());
            adjustment.setAdjustmentNumber(adjustmentDto.getAdjustmentNumber());
            adjustment.setAdjustmentDate(adjustmentDto.getAdjustmentDate());
            adjustment.setUnitPrice(adjustmentDto.getUnitPrice());
            adjustment.setQuantity(adjustmentDto.getQuantity());
            adjustment.setAdjustmentType(adjustmentDto.getAdjustmentType());
            return adjustment;
        }
    }

    public AdjustmentDto toDto(Adjustment adjustment) {
        if (adjustment == null) {
            return null;
        } else {
            AdjustmentDto adjustmentDto = new AdjustmentDto();
            adjustmentDto.setInvoiceId(this.adjustmentInvoiceId(adjustment));
            adjustmentDto.setId(adjustment.getId());
            adjustmentDto.setDescription(adjustment.getDescription());
            adjustmentDto.setAdjustmentNumber(adjustment.getAdjustmentNumber());
            adjustmentDto.setAdjustmentDate(adjustment.getAdjustmentDate());
            adjustmentDto.setUnitPrice(adjustment.getUnitPrice());
            adjustmentDto.setQuantity(adjustment.getQuantity());
            adjustmentDto.setAdjustmentType(adjustment.getAdjustmentType());
            return adjustmentDto;
        }
    }

    public Adjustment partialUpdate(AdjustmentDto adjustmentDto, Adjustment adjustment) {
        if (adjustmentDto == null) {
            return null;
        } else {
            if (adjustment.getInvoice() == null) {
                adjustment.setInvoice(new Invoice(adjustmentDto.getInvoiceId()));
            }

            if (adjustmentDto.getId() != null) {
                adjustment.setId(adjustmentDto.getId());
            }

            if (adjustmentDto.getDescription() != null) {
                adjustment.setDescription(adjustmentDto.getDescription());
            }

            if (adjustmentDto.getAdjustmentNumber() != null) {
                adjustment.setAdjustmentNumber(adjustmentDto.getAdjustmentNumber());
            }

            if (adjustmentDto.getAdjustmentDate() != null) {
                adjustment.setAdjustmentDate(adjustmentDto.getAdjustmentDate());
            }

            if (adjustmentDto.getUnitPrice() != null) {
                adjustment.setUnitPrice(adjustmentDto.getUnitPrice());
            }

            if (adjustmentDto.getQuantity() != null) {
                adjustment.setQuantity(adjustmentDto.getQuantity());
            }

            if (adjustmentDto.getAdjustmentType() != null) {
                adjustment.setAdjustmentType(adjustmentDto.getAdjustmentType());
            }

            return adjustment;
        }
    }

    protected Invoice adjustmentDtoToInvoice(AdjustmentDto adjustmentDto) {
        if (adjustmentDto == null) {
            return null;
        } else {
            Invoice invoice = new Invoice();
            invoice.setId(adjustmentDto.getInvoiceId());
            return invoice;
        }
    }

    private Long adjustmentInvoiceId(Adjustment adjustment) {
        if (adjustment == null) {
            return null;
        } else {
            Invoice invoice = adjustment.getInvoice();
            if (invoice == null) {
                return null;
            } else {
                return invoice.getId();
            }
        }
    }

}
