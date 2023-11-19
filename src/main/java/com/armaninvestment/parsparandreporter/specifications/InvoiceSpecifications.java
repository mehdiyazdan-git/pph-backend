package com.armaninvestment.parsparandreporter.specifications;

import com.armaninvestment.parsparandreporter.dtos.InvoiceFilterDto;
import com.armaninvestment.parsparandreporter.entities.Invoice;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InvoiceSpecifications {
    public static Specification<Invoice> filterInvoices(
            LocalDate startDate,
            LocalDate endDate,
            InvoiceFilterDto invoiceFilterDto
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("issuedDate"), startDate));
            }

            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("issuedDate"), endDate));
            }

            if (invoiceFilterDto != null) {
                if (invoiceFilterDto.getInvoiceNumber() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("invoiceNumber"), invoiceFilterDto.getInvoiceNumber()));
                }

                if (invoiceFilterDto.getIssuedDate() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("issuedDate"), invoiceFilterDto.getIssuedDate()));
                }

                if (invoiceFilterDto.getDueDate() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("dueDate"), invoiceFilterDto.getDueDate()));
                }

                if (invoiceFilterDto.getContractContractDescription() != null) {
                    predicates.add(criteriaBuilder.like(
                            root.get("contractContractDescription"),
                            "%" + invoiceFilterDto.getContractContractDescription() + "%"
                    ));
                }

                if (invoiceFilterDto.getInvoiceStatusId() != null) {
                    predicates.add(criteriaBuilder.equal(
                            root.get("invoiceStatusId"),
                            invoiceFilterDto.getInvoiceStatusId()
                    ));
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}


