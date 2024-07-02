package com.armaninvestment.parsparandreporter.mappers;

import com.armaninvestment.parsparandreporter.dtos.ReportDto;
import com.armaninvestment.parsparandreporter.entities.Report;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring", uses = {ReportItemMapper.class})
public interface ReportMapper {

    Report toEntity(ReportDto reportDto);

    @AfterMapping
    default void linkReportItems(@MappingTarget Report report) {
        report.getReportItems().forEach(reportItem -> reportItem.setReport(report));
    }

    ReportDto toDto(Report report);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Report partialUpdate(ReportDto reportDto, @MappingTarget Report report);
}