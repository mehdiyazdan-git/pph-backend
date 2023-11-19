package com.armaninvestment.parsparandreporter.mappers;

import com.armaninvestment.parsparandreporter.dtos.YearDto;
import com.armaninvestment.parsparandreporter.entities.Year;
import org.springframework.stereotype.Component;

@Component
public class YearMapperImpl implements YearMapper {
    public YearMapperImpl() {
    }

    public Year toEntity(YearDto yearDto) {
        if (yearDto == null) {
            return null;
        } else {
            Year year = new Year();
            year.setId(yearDto.getId());
            year.setName(yearDto.getName());
            return year;
        }
    }

    public YearDto toDto(Year year) {
        if (year == null) {
            return null;
        } else {
            YearDto yearDto = new YearDto();
            yearDto.setId(year.getId());
            yearDto.setName(year.getName());
            return yearDto;
        }
    }

    public Year partialUpdate(YearDto yearDto, Year year) {
        if (yearDto == null) {
            return null;
        } else {
            if (yearDto.getId() != null) {
                year.setId(yearDto.getId());
            }

            if (yearDto.getName() != null) {
                year.setName(yearDto.getName());
            }

            return year;
        }
    }
}
