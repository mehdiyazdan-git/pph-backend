package com.armaninvestment.parsparandreporter.mappers;

import com.armaninvestment.parsparandreporter.dtos.ContractDto;
import com.armaninvestment.parsparandreporter.dtos.ContractItemDto;
import com.armaninvestment.parsparandreporter.entities.Contract;
import com.armaninvestment.parsparandreporter.entities.ContractItem;
import com.armaninvestment.parsparandreporter.entities.Customer;
import com.armaninvestment.parsparandreporter.entities.Year;
import com.armaninvestment.parsparandreporter.exceptions.DuplicateContractNumberException;
import com.armaninvestment.parsparandreporter.repositories.ContractRepository;
import com.armaninvestment.parsparandreporter.repositories.CustomerRepository;
import com.armaninvestment.parsparandreporter.repositories.YearRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Component
public class ContractMapperImpl implements ContractMapper {

    private final ContractItemMapper contractItemMapper;
    private final CustomerRepository customerRepository;
    private final YearRepository yearRepository;
    private final ContractRepository contractRepository;

    @Autowired
    public ContractMapperImpl(ContractItemMapper contractItemMapper, CustomerRepository customerRepository, YearRepository yearRepository,
                              ContractRepository contractRepository) {
        this.contractItemMapper = contractItemMapper;
        this.customerRepository = customerRepository;
        this.yearRepository = yearRepository;
        this.contractRepository = contractRepository;
    }

    private boolean isContractNumberDuplicate(String contractNumber, Long currentContractId) {
        Optional<Contract> existingContract = contractRepository.findByContractNumber(contractNumber);

        // If the contract exists and has a different ID, it's a duplicate
        // If the contract doesn't exist, or it's the current contract being updated, it's not a duplicate
        return existingContract.isPresent() && !existingContract.get().getId().equals(currentContractId);
    }


    public Contract toEntity(ContractDto contractDto) {
        if (contractDto == null) {
            return null;
        } else {
            Contract contract = new Contract();
            contract.setCustomer(this.contractDtoToCustomer(contractDto));
            if (isContractNumberDuplicate(contractDto.getContractNumber(), contract.getId())) {
                throw new DuplicateContractNumberException("شماره قرارداد " + contractDto.getContractNumber() + " تکراریست.");
            }
            contract.setContractNumber(contractDto.getContractNumber());
            contract.setContractDescription(contractDto.getContractDescription());
            contract.setStartDate(contractDto.getStartDate());
            contract.setEndDate(contractDto.getEndDate());
            contract.setAdvancePayment(contractDto.getAdvancePayment());
            contract.setPerformanceBond(contractDto.getPerformanceBond());
            contract.setInsuranceDeposit(contractDto.getInsuranceDeposit());
            contract.setYear(this.contractDtoToYear(contractDto));
            contract.setContractItems(this.contractItemDtoSetToContractItemSet(contractDto.getContractItems()));
            this.linkContractItems(contract);
            return contract;
        }
    }

    public ContractDto toDto(Contract contract) {
        if (contract == null) {
            return null;
        } else {
            ContractDto contractDto = new ContractDto();
            contractDto.setCustomerId(this.contractCustomerId(contract));
            contractDto.setId(contract.getId());
            contractDto.setContractNumber(contract.getContractNumber());
            contractDto.setContractDescription(contract.getContractDescription());
            contractDto.setStartDate(contract.getStartDate());
            contractDto.setEndDate(contract.getEndDate());
            contractDto.setAdvancePayment(contract.getAdvancePayment());
            contractDto.setPerformanceBond(contract.getPerformanceBond());
            contractDto.setInsuranceDeposit(contract.getInsuranceDeposit());
            contractDto.setYearName(this.contractYearName(contract));
            contractDto.setCumulativeInvoiceAmount(this.calculateCumulativeInvoiceAmount(contract.getInvoices()));
            contractDto.setCumulativeInvoiceQuantity(this.calculateCumulativeInvoiceQuantity(contract.getInvoices()));
            contractDto.setCumulativeContractAmount(this.calculateCumulativeContractAmount(contract.getContractItems()));
            contractDto.setCumulativeContractQuantity(this.calculateCumulativeContractQuantity(contract.getContractItems()));
            contractDto.setRemainingContractObligations(this.calculateRemainingContractObligations(contract));
            contractDto.setRemainingContractQuantityObligations(this.calculateRemainingContractQuantityObligations(contract));
            contractDto.setConsumedAdvancePayment(this.calculateConsumedAdvancePayment(contract));
            contractDto.setOutstandingAdvancePayment(this.calculateOutstandingAdvancePayment(contract));
            contractDto.setCumulativeContractPerformanceBonds(this.calculateCumulativeContractPerformanceBonds(contract));
            contractDto.setCumulativeContractInsuranceDeposits(this.calculateCumulativeContractInsuranceDeposits(contract));
            contractDto.setContractItems(this.contractItemSetToContractItemDtoSet(contract.getContractItems()));
            return contractDto;
        }
    }

    public Contract partialUpdate(ContractDto contractDto, Contract contract) {
        if (contractDto == null) {
            return null;
        } else {
            if (contract.getCustomer() == null) {
                contract.setCustomer(new Customer());
            }

            this.contractDtoToCustomer1(contractDto, contract.getCustomer());


            if (contractDto.getContractNumber() != null) {
                contract.setContractNumber(contractDto.getContractNumber());
            }

            if (contractDto.getContractDescription() != null) {
                contract.setContractDescription(contractDto.getContractDescription());
            }

            if (contractDto.getStartDate() != null) {
                contract.setStartDate(contractDto.getStartDate());
            }

            if (contractDto.getEndDate() != null) {
                contract.setEndDate(contractDto.getEndDate());
            }

            if (contractDto.getAdvancePayment() != null) {
                contract.setAdvancePayment(contractDto.getAdvancePayment());
            }

            if (contractDto.getPerformanceBond() != null) {
                contract.setPerformanceBond(contractDto.getPerformanceBond());
            }

            if (contractDto.getInsuranceDeposit() != null) {
                contract.setInsuranceDeposit(contractDto.getInsuranceDeposit());
            }
            if (contractDto.getYearName() != null) {
                contract.setYear(this.contractDtoToYear(contractDto));
            }

            Set<ContractItem> set;
            if (contract.getContractItems() != null) {
                set = this.contractItemDtoSetToContractItemSet(contractDto.getContractItems());
                if (set != null) {
                    contract.getContractItems().clear();
                    contract.getContractItems().addAll(set);
                }
            } else {
                set = this.contractItemDtoSetToContractItemSet(contractDto.getContractItems());
                if (set != null) {
                    contract.setContractItems(set);
                }
            }

            this.linkContractItems(contract);
            return contract;
        }
    }


    protected Customer contractDtoToCustomer(ContractDto contractDto) {
        if (contractDto == null) {
            return null;
        } else {
            return customerRepository.findById(contractDto.getCustomerId()).orElseThrow(() -> new EntityNotFoundException("هیچ مشتری با این شناسه یافت نشد."));
        }
    }

    protected Year contractDtoToYear(ContractDto contractDto) {
        if (contractDto == null) {
            return null;
        } else {
            Long yearName = contractDto.getYearName();
            return yearRepository.findByYearName(yearName).orElseThrow(() -> new EntityNotFoundException("سال با مقدار " + yearName + " یافت نشد."));
        }
    }

    protected Set<ContractItem> contractItemDtoSetToContractItemSet(Set<ContractItemDto> set) {
        if (set == null) {
            return null;
        } else {
            Set<ContractItem> set1 = new HashSet<>(Math.max((int) ((float) set.size() / 0.75F) + 1, 16));

            for (ContractItemDto contractItemDto : set) {
                set1.add(this.contractItemMapper.toEntity(contractItemDto));
            }
            return set1;
        }
    }

    private Long contractCustomerId(Contract contract) {
        if (contract == null) {
            return null;
        } else {
            Customer customer = contract.getCustomer();
            if (customer == null) {
                return null;
            } else {
                return customer.getId();
            }
        }
    }

    private Long contractYearName(Contract contract) {
        if (contract == null) {
            return null;
        } else {
            Year year = contract.getYear();
            if (year == null) {
                return null;
            } else {
                return year.getName();
            }
        }
    }

    protected Set<ContractItemDto> contractItemSetToContractItemDtoSet(Set<ContractItem> set) {
        if (set == null) {
            return null;
        } else {
            Set<ContractItemDto> set1 = new HashSet<>(Math.max((int) ((float) set.size() / 0.75F) + 1, 16));

            for (ContractItem contractItem : set) {
                set1.add(this.contractItemMapper.toDto(contractItem));
            }
            return set1;
        }
    }

    protected void contractDtoToCustomer1(ContractDto contractDto, Customer mappingTarget) {
        if (contractDto != null) {
            if (contractDto.getCustomerId() != null) {
                mappingTarget.setId(contractDto.getCustomerId());
            }

        }
    }
}
