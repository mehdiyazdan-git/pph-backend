package com.armaninvestment.parsparandreporter.mappers;

import com.armaninvestment.parsparandreporter.dtos.AbstractReportDto;
import com.armaninvestment.parsparandreporter.entities.Report;
import org.springframework.stereotype.Component;

@Component
public class AbstractReportMapperImpl implements AbstractReportMapper {
    public AbstractReportMapperImpl() {
    }

    public Report abstractReportDtoToReport(AbstractReportDto abstractReportDto) {
        if (abstractReportDto == null) {
            return null;
        } else {
            Report report = new Report();
            report.setId(abstractReportDto.getId());
            report.setExplanation(abstractReportDto.getExplanation());
            report.setDate(abstractReportDto.getDate());
            return report;
        }
    }

    public AbstractReportDto reportToAbstractReportDto(Report report) {
        if (report == null) {
            return null;
        } else {
            AbstractReportDto abstractReportDto = new AbstractReportDto();
            abstractReportDto.setId(report.getId());
            abstractReportDto.setExplanation(report.getExplanation());
            abstractReportDto.setDate(report.getDate());
            return abstractReportDto;
        }
    }

    public Report updateReportFromAbstractReportDto(AbstractReportDto abstractReportDto, Report report) {
        if (abstractReportDto == null) {
            return report;
        } else {
            if (abstractReportDto.getId() != null) {
                report.setId(abstractReportDto.getId());
            }

            if (abstractReportDto.getExplanation() != null) {
                report.setExplanation(abstractReportDto.getExplanation());
            }

            if (abstractReportDto.getDate() != null) {
                report.setDate(abstractReportDto.getDate());
            }

            return report;
        }
    }
}
