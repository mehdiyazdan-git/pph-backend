package com.armaninvestment.parsparandreporter.repositories;

import com.armaninvestment.parsparandreporter.entities.Contract;
import com.armaninvestment.parsparandreporter.entities.Customer;
import com.armaninvestment.parsparandreporter.entities.Year;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {
    @Query("select c from Contract c where c.contractNumber = :contractNumber")
    Optional<Contract> findByContractNumber(@Param("contractNumber") String contractNumber);

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

    @Query("select c from Contract c where c.year = :year order by c.id asc")
    List<Contract> findAllByYearOrderByIdAsc(@Param("year") Year year);
}