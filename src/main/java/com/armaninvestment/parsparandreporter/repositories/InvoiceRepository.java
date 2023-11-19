package com.armaninvestment.parsparandreporter.repositories;

import com.armaninvestment.parsparandreporter.dtos.ContractAndInvoiceTotalsDTO;
import com.armaninvestment.parsparandreporter.entities.Invoice;
import com.armaninvestment.parsparandreporter.entities.Year;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;


@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long>, JpaSpecificationExecutor<Invoice> {

    @Query("SELECT MAX(i.invoiceNumber) FROM Invoice i")
    Long findMaxInvoiceNumber();

    @Query("SELECT i FROM Invoice i WHERE i.contract.id = :contractId ORDER BY i.invoiceNumber ASC")
    List<Invoice> findAllByContractIdSortedByInvoiceNumberAsc(@Param("contractId") Long contractId);

    @Query("SELECT get_invoice_count_for_contract(cast(:contractId as biginteger))")
    long countInvoicesByContractId(Long contractId);

    @Query("select i from Invoice i where i.year = :year order by i.issuedDate asc")
    List<Invoice> findAllByYearOrderByIssuedDateAsc(@Param("year") Year year);

    @Query("select (count(i) > 0) from Invoice i where i.invoiceNumber = :invoiceNumber and i.year = :year")
    boolean existsByInvoiceNumberAndYear(@Param("invoiceNumber") Long invoiceNumber, @Param("year") Year year);

    @Query("select (count(i) > 0) from Invoice i where i.invoiceNumber = :invoiceNumber and i.issuedDate = :issuedDate")
    boolean existsByInvoiceNumberAndIssuedDate(@Param("invoiceNumber") Long invoiceNumber, @Param("issuedDate") LocalDate issuedDate);

    @Query(value = "select * from get_contract_and_invoice_totals(:contractId,:invoiceNumber)", nativeQuery = true)
    List<Object[]> getContractAndInvoiceTotals(@Param("contractId") Long contractId, @Param("invoiceNumber") Long invoiceNumber);

    default ContractAndInvoiceTotalsDTO getMappedContractAndInvoiceTotals(Long contractId, Long invoiceNumber) {
        List<Object[]> result = getContractAndInvoiceTotals(contractId, invoiceNumber);
        ContractAndInvoiceTotalsDTO dto = new ContractAndInvoiceTotalsDTO();
        for (Object[] row : result) {
            dto.setCumulativeContractAmount((Long) row[0]);
            dto.setCumulativeContractQuantity((Long) row[1]);
            dto.setContractAdvancedPayment((Long) row[2]);
            dto.setContractPerformanceBond((Long) row[3]);
            dto.setContractInsuranceDeposit((Long) row[4]);
            dto.setTotalCommittedContractAmount((Long) row[5]);
            dto.setTotalRemainingContractAmount((Long) row[6]);
            dto.setTotalCommittedContractCount((Long) row[7]);
            dto.setTotalInvoiceCount((Long) row[8]);
            dto.setTotalConsumedContractAdvancedPayment((Long) row[9]);
            dto.setTotalOutstandingContractAdvancedPayment((Long) row[10]);
            dto.setTotalCommitedPerformanceBond((Long) row[11]);
            dto.setTotalRemainingPerformanceBond((Long) row[12]);
            dto.setTotalCommitedInsuranceDeposit((Long) row[13]);
            dto.setTotalRemainingInsuranceDeposit((Long) row[14]);
        }
        return dto;
    }

}