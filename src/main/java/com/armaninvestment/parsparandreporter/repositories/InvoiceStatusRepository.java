package com.armaninvestment.parsparandreporter.repositories;


import com.armaninvestment.parsparandreporter.dtos.InvoiceStatusDto;
import com.armaninvestment.parsparandreporter.entities.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public interface InvoiceStatusRepository extends JpaRepository<InvoiceStatus, Integer> {

    @Query(value = "select * from check_status_exists_by_id(:id)", nativeQuery = true)
    boolean checkStatusExistsById(Integer id);

    @Query("select i from InvoiceStatus i where i.name like concat('%', ?1, '%')")
    List<InvoiceStatus> findByNameContains(String name);

    @Query(nativeQuery = true, value = "SELECT * FROM get_invoice_statuses()")
    List<Object[]> getInvoiceStatuses();

    default List<InvoiceStatusDto> mapToDtoList() {
        return getInvoiceStatuses().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private InvoiceStatusDto mapToDto(Object[] row) {
        return new InvoiceStatusDto(
                (Integer) row[0],
                (String) row[1]
        );
    }

}