package com.armaninvestment.parsparandreporter.repositories;


import com.armaninvestment.parsparandreporter.entities.Payment;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;


@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query(value = "select cast(coalesce(sum(payment_amount),0) as bigint) from payment where customer_id = :customerId", nativeQuery = true)
    Object getTotalPaymentByCustomerId(Long customerId);

    @Query(nativeQuery = true, value = """
                SELECT
                    id,
                    gregorian_to_persian(payment_date + INTERVAL '1 DAY') AS payment_date,
                    payment_descryption,
                    CASE
                        WHEN payment_subject = 'PRODUCT' THEN 'محصول'
                        WHEN payment_subject = 'INSURANCEDEPOSIT' THEN 'سپرده بیمه'
                        WHEN payment_subject = 'PERFORMANCEBOUND' THEN 'حسن انجام کار'
                        WHEN payment_subject = 'ADVANCEDPAYMENT' THEN 'پیش پرداخت'
                        ELSE payment_subject 
                    END AS payment_subject,
                    payment_amount,
                    customer_id
                FROM payment
                WHERE customer_id = :customerId
            """)
    List<Object[]> getPaymentsByCustomerId(@Param("customerId") Long customerId);


    @Query(nativeQuery = true, value = """
                SELECT
                    categories.payment_subject,
                    CAST(COALESCE(SUM(CASE WHEN payment.payment_amount >= 0 THEN payment.payment_amount ELSE 0 END), 0) AS DOUBLE PRECISION) AS sum
                FROM
                    (SELECT 'PERFORMANCEBOUND' AS payment_subject
                     UNION SELECT 'PRODUCT'
                     UNION SELECT 'INSURANCEDEPOSIT'
                     UNION SELECT 'ADVANCEDPAYMENT') AS categories
                LEFT JOIN
                    payment ON categories.payment_subject = payment.payment_subject
                WHERE customer_id = :customerId
                GROUP BY
                    categories.payment_subject
                ORDER BY
                    CASE categories.payment_subject
                        WHEN 'PRODUCT' THEN 1
                        WHEN 'PERFORMANCEBOUND' THEN 2
                        WHEN 'INSURANCEDEPOSIT' THEN 3
                        WHEN 'ADVANCEDPAYMENT' THEN 4
                        ELSE 5
                    END;
            """)
    List<Object[]> getPaymentGroupBySubjectFilterByCustomerId(@Param("customerId") Long customerId);


    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = """
            INSERT INTO public.payment (\s
                        payment_amount, \s
                        payment_date, \s
                        payment_descryption, \s
                        payment_subject, \s
                        customer_id, \s
                        year_id) \s
                        VALUES (:paymentAmount, :paymentDate, :paymentDescription,:paymentSubject , :customerId, :yearId)
            """)
    void insertPayment(
            @Param("paymentAmount") Long paymentAmount,
            @Param("paymentDate") LocalDate paymentDate,
            @Param("paymentDescription") String paymentDescription,
            @Param("paymentSubject") String paymentSubject,
            @Param("customerId") Long customerId,
            @Param("yearId") Long yearId
    );


    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "UPDATE public.payment SET\t" +
            "payment_amount = :paymentAmount, " +
            "payment_date = :paymentDate, " +
            "payment_descryption = :paymentDescription, " +
            "customer_id = :customerId, " +
            "year_id = :yearId ," +
            "payment_subject = :subject  " +
            "WHERE id = :id ")
    void updatePaymentById(@Param("paymentAmount") Long paymentAmount,
                           @Param("paymentDate") LocalDate paymentDate,
                           @Param("paymentDescription") String paymentDescription,
                           @Param("customerId") Long customerId,
                           @Param("yearId") Long yearId,
                           @Param("subject") String subject,
                           @Param("id") Long id
    );


    @Transactional
    @Modifying
    @Query("delete from Payment p where p.id = :id")
    void deleteByPaymentId(@Param("id") @NotNull Long id);

}