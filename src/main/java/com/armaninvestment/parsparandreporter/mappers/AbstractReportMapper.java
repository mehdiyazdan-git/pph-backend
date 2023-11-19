package com.armaninvestment.parsparandreporter.mappers;


import com.armaninvestment.parsparandreporter.dtos.AbstractReportDto;
import com.armaninvestment.parsparandreporter.entities.Report;


public interface AbstractReportMapper {

    Report abstractReportDtoToReport(AbstractReportDto abstractReportDto);

    AbstractReportDto reportToAbstractReportDto(Report report);

    Report updateReportFromAbstractReportDto(AbstractReportDto abstractReportDto, Report report);
}
