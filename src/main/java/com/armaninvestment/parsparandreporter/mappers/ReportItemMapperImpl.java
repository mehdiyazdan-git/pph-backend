package com.armaninvestment.parsparandreporter.mappers;

import com.armaninvestment.parsparandreporter.dtos.ReportItemDto;
import com.armaninvestment.parsparandreporter.entities.Customer;
import com.armaninvestment.parsparandreporter.entities.ReportItem;
import com.armaninvestment.parsparandreporter.entities.WarehouseReceipt;
import com.armaninvestment.parsparandreporter.exceptions.WarehouseReceiptAlreadyAssociatedException;
import com.armaninvestment.parsparandreporter.repositories.CustomerRepository;
import com.armaninvestment.parsparandreporter.repositories.ReportItemRepository;
import com.armaninvestment.parsparandreporter.repositories.WarehouseReceiptRepository;
import com.github.eloyzone.jalalicalendar.DateConverter;
import com.github.eloyzone.jalalicalendar.JalaliDate;
import com.github.eloyzone.jalalicalendar.JalaliDateFormatter;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

@Component
public class ReportItemMapperImpl implements ReportItemMapper {
    private final CustomerRepository customerRepository;
    private final WarehouseReceiptRepository warehouseReceiptRepository;
    private final ReportItemRepository reportItemRepository;

    @Autowired
    public ReportItemMapperImpl(CustomerRepository customerRepository, WarehouseReceiptRepository warehouseReceiptRepository,
                                ReportItemRepository reportItemRepository) {
        this.customerRepository = customerRepository;
        this.warehouseReceiptRepository = warehouseReceiptRepository;
        this.reportItemRepository = reportItemRepository;
    }

    private String convertToPersianDate(LocalDate date) {
        DateConverter dateConverter = new DateConverter();
        JalaliDate jalaliDate = dateConverter.gregorianToJalali(date.getYear(), date.getMonth(), date.getDayOfMonth());
        return jalaliDate.format(new JalaliDateFormatter("yyyy/mm/dd", JalaliDateFormatter.FORMAT_IN_PERSIAN));
    }

    private boolean isWareHouseReceiptDuplicate(Long warehouseReceiptId, Long currentReportItemId) {
        Optional<WarehouseReceipt> optionalWarehouseReceipt = warehouseReceiptRepository.findById(warehouseReceiptId);
        return optionalWarehouseReceipt.isPresent() && optionalWarehouseReceipt.get().getReportItem() != null && !Objects.equals(optionalWarehouseReceipt.get().getReportItem().getId(), currentReportItemId);
    }

    public ReportItem toEntity(ReportItemDto reportItemDto) {
        if (reportItemDto == null) {
            return null;
        } else {
            ReportItem reportItem = new ReportItem();
            WarehouseReceipt warehouseReceipt = this.mapToWarehouseReceipt(reportItemDto);
            if (isWareHouseReceiptDuplicate(reportItemDto.getWarehouseReceiptId(), reportItemDto.getId())) {
                throw new WarehouseReceiptAlreadyAssociatedException("حواله " + warehouseReceipt.getWarehouseReceiptNumber() + " با گزارش  دیگری در ارتباط می باشد.");
            }
            reportItem.setWarehouseReceipt(warehouseReceipt);
            reportItem.setCustomer(this.mapToCustomer(reportItemDto));
            reportItem.setId(reportItemDto.getId());
            reportItem.setUnitPrice(reportItemDto.getUnitPrice());
            reportItem.setQuantity(reportItemDto.getQuantity());
            return reportItem;
        }
    }

    public ReportItemDto toDto(ReportItem reportItem) {
        if (reportItem == null) {
            return null;
        } else {
            ReportItemDto reportItemDto = new ReportItemDto();
            reportItemDto.setWarehouseReceiptId(this.reportItemWarehouseReceiptId(reportItem));
            reportItemDto.setCustomerId(this.reportItemCustomerId(reportItem));
            reportItemDto.setId(reportItem.getId());
            reportItemDto.setUnitPrice(reportItem.getUnitPrice());
            reportItemDto.setQuantity(reportItem.getQuantity());
            return reportItemDto;
        }
    }

    public ReportItem partialUpdate(ReportItemDto reportItemDto, ReportItem reportItem) {
        if (reportItemDto == null) {
            return null;
        } else {
            if (reportItem.getWarehouseReceipt() != null) {
                WarehouseReceipt warehouseReceipt = this.mapToWarehouseReceipt(reportItemDto);
                if (isWareHouseReceiptDuplicate(reportItemDto.getWarehouseReceiptId(), reportItemDto.getId())) {
                    throw new WarehouseReceiptAlreadyAssociatedException("حواله " + warehouseReceipt.getWarehouseReceiptNumber() + " با گزارش " + convertToPersianDate(warehouseReceipt.getReportItem().getReport().getDate()) + "در ارتباط می باشد.");
                }
                reportItem.setWarehouseReceipt(this.mapToWarehouseReceipt(reportItemDto));
            }

            if (reportItem.getCustomer() != null) {
                reportItem.setCustomer(this.mapToCustomer(reportItemDto));
            }

            if (reportItemDto.getId() != null) {
                reportItem.setId(reportItemDto.getId());
            }

            if (reportItemDto.getUnitPrice() != null) {
                reportItem.setUnitPrice(reportItemDto.getUnitPrice());
            }

            if (reportItemDto.getQuantity() != null) {
                reportItem.setQuantity(reportItemDto.getQuantity());
            }

            return reportItem;
        }
    }

    protected WarehouseReceipt mapToWarehouseReceipt(ReportItemDto reportItemDto) {
        if (reportItemDto == null) {
            return null;
        } else {
            Long warehouseReceiptId = reportItemDto.getWarehouseReceiptId();
            return warehouseReceiptRepository.findById(warehouseReceiptId).orElseThrow(() -> new EntityNotFoundException("حواله با شناسه " + warehouseReceiptId + " یافت نشد."));
        }
    }

    protected Customer mapToCustomer(ReportItemDto reportItemDto) {
        if (reportItemDto == null) {
            return null;
        } else {
            Long customerId = reportItemDto.getCustomerId();
            return customerRepository.findById(customerId).orElseThrow(() -> new EntityNotFoundException("مشتری با شناسه " + customerId + " یافت نشد."));
        }
    }

    private Long reportItemWarehouseReceiptId(ReportItem reportItem) {
        if (reportItem == null) {
            return null;
        } else {
            WarehouseReceipt warehouseReceipt = reportItem.getWarehouseReceipt();
            if (warehouseReceipt == null) {
                return null;
            } else {
                Long id = warehouseReceipt.getId();
                return id == null ? null : id;
            }
        }
    }

    private Long reportItemCustomerId(ReportItem reportItem) {
        if (reportItem == null) {
            return null;
        } else {
            Customer customer = reportItem.getCustomer();
            if (customer == null) {
                return null;
            } else {
                Long id = customer.getId();
                return id == null ? null : id;
            }
        }

    }

}
