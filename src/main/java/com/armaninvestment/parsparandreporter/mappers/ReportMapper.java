package com.armaninvestment.parsparandreporter.mappers;

import com.armaninvestment.parsparandreporter.dtos.ReportDto;
import com.armaninvestment.parsparandreporter.entities.Report;


public interface ReportMapper {
    Report toEntity(ReportDto reportDto);

    default void linkReportItems(Report report) {
        report.getReportItems().forEach(reportItem -> reportItem.setReport(report));
    }

    ReportDto toDto(Report report);

    Report partialUpdate(ReportDto reportDto, Report report);
}