package com.armaninvestment.parsparandreporter.mappers;

import com.armaninvestment.parsparandreporter.dtos.WarehouseReceiptDto;
import com.armaninvestment.parsparandreporter.dtos.WarehouseReceiptItemDto;
import com.armaninvestment.parsparandreporter.entities.*;
import com.armaninvestment.parsparandreporter.exceptions.WareHouseReceiptRepetitiveByNumberAndDateException;
import com.armaninvestment.parsparandreporter.exceptions.WareHouseReceiptRepetitiveByNumberAndYearException;
import com.armaninvestment.parsparandreporter.repositories.CustomerRepository;
import com.armaninvestment.parsparandreporter.repositories.WarehouseReceiptRepository;
import com.armaninvestment.parsparandreporter.repositories.YearRepository;
import com.github.eloyzone.jalalicalendar.DateConverter;
import com.github.eloyzone.jalalicalendar.JalaliDate;
import com.github.eloyzone.jalalicalendar.JalaliDateFormatter;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class WarehouseReceiptMapperImpl implements WarehouseReceiptMapper {

    private final WarehouseReceiptItemMapper warehouseReceiptItemMapper;
    private final CustomerRepository customerRepository;
    private final YearRepository yearRepository;
    private final WarehouseReceiptRepository warehouseReceiptRepository;

    @Autowired
    public WarehouseReceiptMapperImpl(WarehouseReceiptItemMapper warehouseReceiptItemMapper,
                                      CustomerRepository customerRepository, YearRepository yearRepository,
                                      WarehouseReceiptRepository warehouseReceiptRepository) {
        this.warehouseReceiptItemMapper = warehouseReceiptItemMapper;
        this.customerRepository = customerRepository;
        this.yearRepository = yearRepository;
        this.warehouseReceiptRepository = warehouseReceiptRepository;
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


    public WarehouseReceipt toEntity(WarehouseReceiptDto warehouseReceiptDto) {

        Long number = warehouseReceiptDto.getWarehouseReceiptNumber();
        LocalDate date = warehouseReceiptDto.getWarehouseReceiptDate();
        Long yearName = warehouseReceiptDto.getYearName();


        DateConverter dateConverter = new DateConverter();
        JalaliDate jalaliDate = dateConverter.gregorianToJalali(date.getYear(), date.getMonth(), date.getDayOfMonth());
        String result = jalaliDate.format(new JalaliDateFormatter("yyyy/mm/dd", JalaliDateFormatter.FORMAT_IN_PERSIAN));

        if (warehouseReceiptRepository.existsWarehouseReceiptByWarehouseReceiptNumberAndWarehouseReceiptDate(number, date)) {
            throw new WareHouseReceiptRepetitiveByNumberAndDateException("حواله با تاریخ " + result + "و شماره " + convertToPersianDigits(number) + " قبلا ثبت شده است.");
        }
        if (warehouseReceiptRepository.existsWarehouseReceiptByWarehouseReceiptNumberAndYear(number, getYear(warehouseReceiptDto.getYearName()))) {
            throw new WareHouseReceiptRepetitiveByNumberAndYearException(
                    "حواله با شماره " + convertToPersianDigits(number)
                            + "در سال " + convertToPersianDigits(yearName) + " قبلا ثبت شده است.");
        }

        if (warehouseReceiptDto == null) {
            return null;
        } else {
            WarehouseReceipt warehouseReceipt = new WarehouseReceipt();
            warehouseReceipt.setCustomer(this.toCustomerEntity(warehouseReceiptDto));
            warehouseReceipt.setId(warehouseReceiptDto.getId());
            warehouseReceipt.setWarehouseReceiptNumber(warehouseReceiptDto.getWarehouseReceiptNumber());
            warehouseReceipt.setWarehouseReceiptDate(warehouseReceiptDto.getWarehouseReceiptDate());
            warehouseReceipt.setWarehouseReceiptDescription(warehouseReceiptDto.getWarehouseReceiptDescription());
            warehouseReceipt.setYear(this.getYear(yearName));
            warehouseReceipt.setWarehouseReceiptItems(this.warehouseReceiptItemDtoListToWarehouseReceiptItemList(warehouseReceiptDto.getWarehouseReceiptItems()));
            this.linkWarehouseReceiptItems(warehouseReceipt);
            return warehouseReceipt;
        }
    }

    public WarehouseReceiptDto toDto(WarehouseReceipt warehouseReceipt) {
        if (warehouseReceipt == null) {
            return null;
        } else {
            WarehouseReceiptDto warehouseReceiptDto = new WarehouseReceiptDto();
            warehouseReceiptDto.setCustomerId(this.warehouseReceiptCustomerId(warehouseReceipt));
            warehouseReceiptDto.setId(warehouseReceipt.getId());
            warehouseReceiptDto.setWarehouseReceiptNumber(warehouseReceipt.getWarehouseReceiptNumber());
            warehouseReceiptDto.setWarehouseReceiptDate(warehouseReceipt.getWarehouseReceiptDate());
            warehouseReceiptDto.setWarehouseReceiptDescription(warehouseReceipt.getWarehouseReceiptDescription());
            warehouseReceiptDto.setYearName(this.getReceiptYearId(warehouseReceipt));
            warehouseReceiptDto.setInvoiceNumber(this.getInvoiceNumber(warehouseReceipt));
            warehouseReceiptDto.setReportDate(this.getReportDate(warehouseReceipt));
            warehouseReceiptDto.setWarehouseReceiptItems(this.warehouseReceiptItemListToWarehouseReceiptItemDtoList(warehouseReceipt.getWarehouseReceiptItems()));
            return warehouseReceiptDto;
        }
    }

    public WarehouseReceipt partialUpdate(WarehouseReceiptDto warehouseReceiptDto, WarehouseReceipt warehouseReceipt) {
        if (warehouseReceiptDto == null) {
            return null;
        } else {
            if (warehouseReceipt.getCustomer() == null) {
                warehouseReceipt.setCustomer(new Customer());
            }
            if (warehouseReceiptDto.getId() != null) {
                warehouseReceipt.setId(warehouseReceiptDto.getId());
            }
            if (warehouseReceiptDto.getCustomerId() != null) {
                warehouseReceipt.setCustomer(this.toCustomerEntity(warehouseReceiptDto));
            }

            if (warehouseReceiptDto.getWarehouseReceiptNumber() != null) {
                warehouseReceipt.setWarehouseReceiptNumber(warehouseReceiptDto.getWarehouseReceiptNumber());
            }

            if (warehouseReceiptDto.getWarehouseReceiptDate() != null) {
                warehouseReceipt.setWarehouseReceiptDate(warehouseReceiptDto.getWarehouseReceiptDate());
            }

            if (warehouseReceiptDto.getWarehouseReceiptDescription() != null) {
                warehouseReceipt.setWarehouseReceiptDescription(warehouseReceiptDto.getWarehouseReceiptDescription());
            }
            if (warehouseReceiptDto.getYearName() != null) {
                warehouseReceipt.setYear(this.getYear(warehouseReceiptDto.getYearName()));
            }

            List list;
            if (warehouseReceipt.getWarehouseReceiptItems() != null) {
                list = this.warehouseReceiptItemDtoListToWarehouseReceiptItemList(warehouseReceiptDto.getWarehouseReceiptItems());
                if (list != null) {
                    warehouseReceipt.getWarehouseReceiptItems().clear();
                    warehouseReceipt.getWarehouseReceiptItems().addAll(list);
                }
            } else {
                list = this.warehouseReceiptItemDtoListToWarehouseReceiptItemList(warehouseReceiptDto.getWarehouseReceiptItems());
                if (list != null) {
                    warehouseReceipt.setWarehouseReceiptItems(list);
                }
            }

            this.linkWarehouseReceiptItems(warehouseReceipt);
            return warehouseReceipt;
        }
    }

    protected Customer toCustomerEntity(WarehouseReceiptDto warehouseReceiptDto) {
        if (warehouseReceiptDto == null) {
            return null;
        } else {
            Long customerId = warehouseReceiptDto.getCustomerId();
            return customerRepository.findById(customerId).orElseThrow(() -> new EntityNotFoundException("مشتری با شناسه " + customerId + "یافت نشد."));
        }
    }

    protected Year getYear(Long yearName) {
        Optional<Year> optionalYear = yearRepository.findByYearName(yearName);
        return optionalYear.orElseThrow(() -> new EntityNotFoundException("سال با شناسه " + yearName + " یافت نشد."));
    }

    protected LocalDate getReportDate(WarehouseReceipt warehouseReceipt) {
        ReportItem reportItem = warehouseReceipt.getReportItem();
        if (reportItem != null) {
            return reportItem.getReport().getDate();
        }
        return null;
    }

    protected Long getInvoiceNumber(WarehouseReceipt warehouseReceipt) {
        InvoiceItem invoiceItem = warehouseReceipt.getInvoiceItem();
        if (invoiceItem != null) {
            return invoiceItem.getInvoice().getInvoiceNumber();
        }
        return null;
    }

    protected List<WarehouseReceiptItem> warehouseReceiptItemDtoListToWarehouseReceiptItemList(List<WarehouseReceiptItemDto> list) {
        if (list == null) {
            return null;
        } else {
            List<WarehouseReceiptItem> list1 = new ArrayList<>(list.size());

            for (WarehouseReceiptItemDto warehouseReceiptItemDto : list) {
                list1.add(this.warehouseReceiptItemMapper.toEntity(warehouseReceiptItemDto));
            }

            return list1;
        }
    }

    private Long warehouseReceiptCustomerId(WarehouseReceipt warehouseReceipt) {
        if (warehouseReceipt == null) {
            return null;
        } else {
            Customer customer = warehouseReceipt.getCustomer();
            if (customer == null) {
                return null;
            } else {
                Long id = customer.getId();
                return id == null ? null : id;
            }
        }
    }

    private Long getReceiptYearId(WarehouseReceipt warehouseReceipt) {
        if (warehouseReceipt == null) {
            return null;
        } else {
            Year year = warehouseReceipt.getYear();
            if (year == null) {
                return null;
            } else {
                return year.getId();
            }
        }
    }

    protected List<WarehouseReceiptItemDto> warehouseReceiptItemListToWarehouseReceiptItemDtoList(List<WarehouseReceiptItem> list) {
        if (list == null) {
            return null;
        } else {
            List<WarehouseReceiptItemDto> list1 = new ArrayList<>(list.size());

            for (WarehouseReceiptItem warehouseReceiptItem : list) {
                list1.add(this.warehouseReceiptItemMapper.toDto(warehouseReceiptItem));
            }

            return list1;
        }
    }

    protected void warehouseReceiptDtoToCustomer1(WarehouseReceiptDto warehouseReceiptDto, Customer mappingTarget) {
        if (warehouseReceiptDto != null) {
            if (warehouseReceiptDto.getCustomerId() != null) {
                mappingTarget.setId(warehouseReceiptDto.getCustomerId());
            }

        }
    }
}
