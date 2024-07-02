package com.armaninvestment.parsparandreporter.dtos;

import com.armaninvestment.parsparandreporter.dtos.report.AdjustmentReportDto;
import com.armaninvestment.parsparandreporter.dtos.report.NotInvoicedReportDto;
import com.armaninvestment.parsparandreporter.dtos.report.PaymentReportDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ClientSummaryResult {
    private List<ClientSummaryDTO> clientSummaryList;
    private NotInvoicedReportDto notInvoicedReportDto;
    private AdjustmentReportDto adjustmentReportDto;
    private EstablishmentDto establishmentDto;
    private PaymentReportDto totalPaymentByCustomerId;
}
