package com.armaninvestment.parsparandreporter.mappers;

import com.armaninvestment.parsparandreporter.dtos.YearDto;
import com.armaninvestment.parsparandreporter.entities.Year;


public interface YearMapper {
    Year toEntity(YearDto yearDto);

    YearDto toDto(Year year);

    Year partialUpdate(YearDto yearDto, Year year);
}