package com.armaninvestment.parsparandreporter.mappers;

import com.armaninvestment.parsparandreporter.dtos.ReportItemDto;
import com.armaninvestment.parsparandreporter.entities.ReportItem;


public interface ReportItemMapper {
    ReportItem toEntity(ReportItemDto reportItemDto);

    ReportItemDto toDto(ReportItem reportItem);

    ReportItem partialUpdate(ReportItemDto reportItemDto, ReportItem reportItem);
}