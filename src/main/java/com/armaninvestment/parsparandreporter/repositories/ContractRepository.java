package com.armaninvestment.parsparandreporter.repositories;

import com.armaninvestment.parsparandreporter.dtos.ContractListDto;
import com.armaninvestment.parsparandreporter.dtos.dropdowns.ContractDropDownDto;
import com.armaninvestment.parsparandreporter.entities.Contract;
import com.armaninvestment.parsparandreporter.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {

    @Query(value = "select * from get_all_invoices_by_contract_id(:contractId)", nativeQuery = true)
    List<Object[]> getAllInvoicesByContractId(Long contractId);

    @Query("select (count(c) > 0) from Contract c where c.id = :id")
    boolean existsContractById(@Param("id") Long id);

    @Query("select c from Contract c where c.contractNumber = :contractNumber")
    Optional<Contract> findByContractNumber(@Param("contractNumber") String contractNumber);

    @Query(value = "SELECT find_by_contract_number(:contractNumber)", nativeQuery = true)
    Long getContractIdByContractNumber(String contractNumber);

    @Query("select c from Contract c where c.customer.id = :customerId")
    List<Contract> findByCustomerId(@Param("customerId") Long customerId);

    @Query("select c from Contract c where c.customer = :customer")
    List<Contract> findAllByCustomer(@Param("customer") Customer customer);

    @Query("select c from Contract c where c.contractDescription like concat('%', :contractDescription, '%')")
    List<Contract> findByContractDescriptionContains(@Param("contractDescription") String contractDescription);

    @Query("select c from Contract c where c.contractDescription like concat('%', :contractDescription, '%') and c.customer.id = :customerId")
    List<Contract> findByContractDescriptionContainsAndCustomerId(
            @Param("contractDescription") String contractDescription,
            @Param("customerId") Long customerId);

    @Query(value = "select * from get_all_contracts_by_year_name(:yearName)", nativeQuery = true)
    List<Object[]> getContractsByYear(Long yearName);

    default List<ContractListDto> mapToContractListDto(Long yearName) {
        List<Object[]> results = getContractsByYear(yearName);
        List<ContractListDto> list = new ArrayList<>();
        for (Object[] obj : results) {
            ContractListDto dto = new ContractListDto();
            dto.setId((Long) obj[0]);
            dto.setContractNumber((String) obj[1]);
            dto.setContractDescription((String) obj[2]);
            dto.setStartDate(((Date) obj[3]).toLocalDate());
            dto.setEndDate(((Date) obj[4]).toLocalDate());
            dto.setCustomerName((String) obj[5]);
            dto.setTotalAmount((BigDecimal) obj[6]);
            dto.setTotalQuantity((BigDecimal) obj[7]);
            list.add(dto);
        }
        return list;
    }

    @Query(nativeQuery = true, value = "SELECT * FROM get_contract_by_id(:contractId)")
    List<Object[]> getContractById(@Param("contractId") Long contractId);

    @Query(nativeQuery = true, value = "SELECT CAST(contract_description AS VARCHAR) FROM contracts WHERE contract_number = :contractNumber")
    String findContractDescriptionByContractNumber(@Param("contractNumber") String contractNumber);


    @Query(nativeQuery = true, value = "SELECT * FROM get_contract_items_by_contract_id(:contractId)")
    List<Object[]> getContractItemsByContractId(@Param("contractId") Long contractId);

    @Query(nativeQuery = true, value = "SELECT * FROM get_contract_dropdown(:customerId,:yearName)")
    List<Object[]> getContractDropDown(@Param("customerId") Long customerId, @Param("yearName") Long yearName);

    default List<ContractDropDownDto> mapToDtoList(Long customerId, Long yearName) {
        return getContractDropDown(customerId, yearName).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private ContractDropDownDto mapToDto(Object[] row) {
        return new ContractDropDownDto(
                (Long) row[0],
                (String) row[1]
        );
    }

    @Query(value = "select * from search_contract_by_description_keywords(:queryParam,:customerId,:yearName)", nativeQuery = true)
    List<Object[]> searchContractByDescriptionKeywords(@Param("queryParam") String queryParam, @Param("customerId") Long customerId, @Param("yearName") Long yearName);

    @Query(value = "select * from get_contract_and_contract_details_by_id(:contractId)", nativeQuery = true)
    List<Object[]> getContractAndContractDetailsById(@Param("contractId") Long customerId);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM public.invoice WHERE contract_id = ?1)", nativeQuery = true)
    boolean existsContractByIdAndInvoicesIsNotEmpty(Long contractId);

    @Query(value = "SELECT c FROM Contract c WHERE c.customer.id = :customerId ORDER BY c.id DESC limit 1")
    Optional<Contract> findLastContractByCustomerId(@Param("customerId") Long customerId);


}