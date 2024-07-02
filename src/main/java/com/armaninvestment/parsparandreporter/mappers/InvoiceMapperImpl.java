package com.armaninvestment.parsparandreporter.mappers;

import com.armaninvestment.parsparandreporter.dtos.InvoiceDto;
import com.armaninvestment.parsparandreporter.dtos.InvoiceItemDto;
import com.armaninvestment.parsparandreporter.entities.*;
import com.armaninvestment.parsparandreporter.enums.SalesType;
import com.armaninvestment.parsparandreporter.exceptions.InvoiceExistByNumberAndIssuedDateException;
import com.armaninvestment.parsparandreporter.repositories.*;
import com.github.eloyzone.jalalicalendar.DateConverter;
import com.github.eloyzone.jalalicalendar.JalaliDate;
import com.github.eloyzone.jalalicalendar.JalaliDateFormatter;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Component
public class InvoiceMapperImpl implements InvoiceMapper {
    private final InvoiceItemMapper invoiceItemMapper;
    private final InvoiceStatusRepository invoiceStatusRepository;
    private final ContractRepository contractRepository;
    private final YearRepository yearRepository;
    private final InvoiceRepository invoiceRepository;

    private final CustomerRepository customerRepository;

    @Autowired
    public InvoiceMapperImpl(InvoiceItemMapper invoiceItemMapper, InvoiceStatusRepository invoiceStatusRepository, ContractRepository contractRepository, YearRepository yearRepository,
                             InvoiceRepository invoiceRepository, CustomerRepository customerRepository) {
        this.invoiceItemMapper = invoiceItemMapper;
        this.invoiceStatusRepository = invoiceStatusRepository;
        this.contractRepository = contractRepository;
        this.yearRepository = yearRepository;
        this.invoiceRepository = invoiceRepository;
        this.customerRepository = customerRepository;
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

    public boolean doesInvoiceExist(Long invoiceNumber, LocalDate issuedDate) {
        return invoiceRepository.checkInvoiceExists(invoiceNumber, java.sql.Date.valueOf(issuedDate));
    }

    public Invoice toEntity(InvoiceDto invoiceDto) {
        Long number = invoiceDto.getInvoiceNumber();
        LocalDate date = invoiceDto.getIssuedDate();
        Long yearName = invoiceDto.getYearName();

        DateConverter dateConverter = new DateConverter();
        JalaliDate jalaliDate = dateConverter.gregorianToJalali(date.getYear(), date.getMonth(), date.getDayOfMonth());
        String result = jalaliDate.format(new JalaliDateFormatter("yyyy/mm/dd", JalaliDateFormatter.FORMAT_IN_PERSIAN));

        if (doesInvoiceExist(number, date)) {
            throw new InvoiceExistByNumberAndIssuedDateException(
                    "فاکتور با تاریخ " + result + "و شماره " + convertToPersianDigits(number) + " قبلا ثبت شده است.");
        }

        if (invoiceDto == null) {
            return null;
        } else {
            Invoice invoice = new Invoice();
            invoice.setInvoiceStatus(this.getInvoiceStatus(invoiceDto.getInvoiceStatusId()));
            invoice.setSalesType(invoiceDto.getSalesType());
            invoice.setCustomer(this.getCustomer(invoiceDto.getCustomerId()));
            invoice.setInvoiceNumber(invoiceDto.getInvoiceNumber());
            invoice.setIssuedDate(invoiceDto.getIssuedDate());
            invoice.setDueDate(invoiceDto.getDueDate());
            invoice.setYear(this.getYear(invoiceDto.getYearName()));
            invoice.setAdvancedPayment(invoiceDto.getAdvancedPayment());
            invoice.setPerformanceBound(invoiceDto.getPerformanceBound());
            invoice.setInsuranceDeposit(invoiceDto.getInsuranceDeposit());
            invoice.setInvoiceItems(this.convertDtoSetToEntitySet(invoiceDto.getInvoiceItems()));

            // Set the contract only if the sales type is CONTRACTUAL_SALES
            if (SalesType.CONTRACTUAL_SALES.equals(invoiceDto.getSalesType())) {
                Optional<Contract> optionalContract = contractRepository.findById(invoiceDto.getContractId());
                if (optionalContract.isEmpty()) {
                    throw new IllegalArgumentException("شناسه قرارداد نمی تواند خالی باشد.");
                }
                invoice.setContract(optionalContract.get());
            } else {
                // If the sales type is CASH_SALES, make sure the contract-related fields are not set
                if (invoiceDto.getContractId() != null) {
                    throw new IllegalArgumentException("قرارداد نمی تواند در فروش نقدی مشخص باشد.");
                }
            }

            this.linkInvoiceItems(invoice);
            return invoice;
        }
    }


    public InvoiceDto toDto(Invoice invoice) {
        if (invoice == null) {
            return null;
        } else {
            InvoiceDto invoiceDto = new InvoiceDto();
            invoiceDto.setInvoiceStatusId(this.getInvoiceStatusId(invoice));
            invoiceDto.setContractId(this.getInvoiceContractId(invoice));
            invoiceDto.setSalesType(invoice.getSalesType());
            invoiceDto.setCustomerId(invoice.getCustomer().getId());
            invoiceDto.setId(invoice.getId());
            invoiceDto.setInvoiceNumber(invoice.getInvoiceNumber());
            invoiceDto.setIssuedDate(invoice.getIssuedDate());
            invoiceDto.setDueDate(invoice.getDueDate());
            invoiceDto.setYearName(this.getInvoiceYearName(invoice));
            invoiceDto.setAdvancedPayment(invoice.getAdvancedPayment());
            invoiceDto.setPerformanceBound(invoice.getPerformanceBound());
            invoiceDto.setInsuranceDeposit(invoice.getInsuranceDeposit());
            invoiceDto.setInvoiceItems(this.convertEntitySetToDtoSet(invoice.getInvoiceItems()));
            return invoiceDto;
        }
    }

    public Invoice partialUpdate(InvoiceDto invoiceDto, Invoice invoice) {
        if (invoiceDto == null) {
            return null;
        } else {

            if (invoiceDto.getId() != null) {
                invoice.setId(invoiceDto.getId());
            }
            if (invoiceDto.getInvoiceStatusId() != null) {
                invoice.setInvoiceStatus(this.getInvoiceStatus(invoiceDto.getInvoiceStatusId()));
            }

            // Set the contract only if the sales type is CONTRACTUAL_SALES
            if (SalesType.CONTRACTUAL_SALES.equals(invoiceDto.getSalesType())) {
                boolean existsContractById = contractRepository.existsContractById(invoiceDto.getContractId());
                if (!existsContractById) {
                    throw new IllegalArgumentException("شناسه قرارداد نمی تواند خالی باشد.");
                }
                invoice.setContract(new Contract(invoiceDto.getContractId()));
            } else {
                // If the sales type is CASH_SALES, make sure the contract-related fields are not set
                if (invoiceDto.getContractId() != null) {
                    throw new IllegalArgumentException("قرارداد نمی تواند در فروش نقدی مشخص باشد.");
                }
            }
            if (invoiceDto.getSalesType() != null) {
                invoice.setSalesType(invoiceDto.getSalesType());
            }
            if (invoiceDto.getCustomerId() != null) {
                invoice.setCustomer(this.getCustomer(invoiceDto.getCustomerId()));
            }

            if (invoiceDto.getInvoiceNumber() != null) {
                invoice.setInvoiceNumber(invoiceDto.getInvoiceNumber());
            }
            if (invoiceDto.getAdvancedPayment() != null) {
                invoice.setAdvancedPayment(invoiceDto.getAdvancedPayment());
            }
            if (invoiceDto.getPerformanceBound() != null) {
                invoice.setPerformanceBound(invoiceDto.getPerformanceBound());
            }
            if (invoiceDto.getInsuranceDeposit() != null) {
                invoice.setInsuranceDeposit(invoiceDto.getInsuranceDeposit());
            }

            if (invoiceDto.getIssuedDate() != null) {
                invoice.setIssuedDate(invoiceDto.getIssuedDate());
            }

            if (invoiceDto.getDueDate() != null) {
                invoice.setDueDate(invoiceDto.getDueDate());
            }
            if (invoiceDto.getYearName() != null) {
                invoice.setYear(this.getYear(invoiceDto.getYearName()));
            }

            Set<InvoiceItem> set;
            if (invoice.getInvoiceItems() != null) {
                set = this.convertDtoSetToEntitySet(invoiceDto.getInvoiceItems());
                if (set != null) {
                    invoice.getInvoiceItems().clear();
                    invoice.getInvoiceItems().addAll(set);
                }
            } else {
                set = this.convertDtoSetToEntitySet(invoiceDto.getInvoiceItems());
                if (set != null) {
                    invoice.setInvoiceItems(set);
                }
            }

            this.linkInvoiceItems(invoice);
            return invoice;
        }
    }

    protected InvoiceStatus getInvoiceStatus(Integer invoiceStatusId) {
        boolean statusExistsById = invoiceStatusRepository.checkStatusExistsById(invoiceStatusId);
        if (!statusExistsById) throw new EntityNotFoundException("وضعیت با شناسه " + invoiceStatusId + " یافت نشد.");
        return new InvoiceStatus(invoiceStatusId);
    }

    protected Customer getCustomer(Long customerId) {
//        boolean customerExistsById = customerRepository.checkCustomerExistsById(customerId);
//        if (!customerExistsById) throw new EntityNotFoundException("مشتری با شناسه " + customerId + " یافت نشد.");
        return new Customer(customerId);
    }

    protected Year getYear(Long yearName) {
        Optional<Year> optionalYear = yearRepository.findByYearName(yearName);
        return optionalYear.orElseThrow(() -> new EntityNotFoundException("سال با مقدار " + yearName + " یافت نشد."));
    }

    protected Set<InvoiceItem> convertDtoSetToEntitySet(Set<InvoiceItemDto> invoiceItemDtoSet) {
        if (invoiceItemDtoSet == null) {
            return null;
        } else {
            Set<InvoiceItem> invoiceItemSet = new HashSet<>(Math.max((int) ((float) invoiceItemDtoSet.size() / 0.75F) + 1, 16));

            for (InvoiceItemDto invoiceItemDto : invoiceItemDtoSet) {
                invoiceItemSet.add(this.invoiceItemMapper.toEntity(invoiceItemDto));
            }

            return invoiceItemSet;
        }
    }


    private Integer getInvoiceStatusId(Invoice invoice) {
        if (invoice == null) {
            return null;
        } else {
            InvoiceStatus invoiceStatus = invoice.getInvoiceStatus();
            if (invoiceStatus == null) {
                return null;
            } else {
                return invoiceStatus.getId();
            }
        }
    }

    private Long getInvoiceYearName(Invoice invoice) {
        if (invoice == null) {
            return null;
        } else {
            Year year = invoice.getYear();
            if (year == null) {
                return null;
            } else {
                return year.getName();
            }
        }
    }

    private Long getInvoiceContractId(Invoice invoice) {
        if (invoice == null) {
            return null;
        } else {
            Contract contract = invoice.getContract();
            if (contract == null) {
                return null;
            } else {
                return contract.getId();
            }
        }
    }

    protected Set<InvoiceItemDto> convertEntitySetToDtoSet(Set<InvoiceItem> invoiceItemSet) {
        if (invoiceItemSet == null) {
            return null;
        } else {
            Set<InvoiceItemDto> invoiceItemDtoSet = new HashSet<>(Math.max((int) ((float) invoiceItemSet.size() / 0.75F) + 1, 16));

            for (InvoiceItem invoiceItem : invoiceItemSet) {
                invoiceItemDtoSet.add(this.invoiceItemMapper.toDto(invoiceItem));
            }

            return invoiceItemDtoSet;
        }
    }
}
