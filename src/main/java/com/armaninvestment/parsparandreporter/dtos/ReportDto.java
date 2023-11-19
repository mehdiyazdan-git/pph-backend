package com.armaninvestment.parsparandreporter.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for {@link com.armaninvestment.parsparandreporter.entities.Report}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportDto implements Serializable {
    private Long id;
    private String explanation;
    private LocalDate date;
    private List<ReportItemDto> reportItems = new ArrayList<>();
    private Long yearName;
}