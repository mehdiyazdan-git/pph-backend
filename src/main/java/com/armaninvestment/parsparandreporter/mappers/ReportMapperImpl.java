package com.armaninvestment.parsparandreporter.mappers;

import com.armaninvestment.parsparandreporter.dtos.ReportDto;
import com.armaninvestment.parsparandreporter.dtos.ReportItemDto;
import com.armaninvestment.parsparandreporter.entities.Report;
import com.armaninvestment.parsparandreporter.entities.ReportItem;
import com.armaninvestment.parsparandreporter.entities.Year;
import com.armaninvestment.parsparandreporter.repositories.YearRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class ReportMapperImpl implements ReportMapper {

    private final ReportItemMapper reportItemMapper;
    private final YearRepository yearRepository;

    @Autowired
    public ReportMapperImpl(ReportItemMapper reportItemMapper, YearRepository yearRepository) {
        this.reportItemMapper = reportItemMapper;
        this.yearRepository = yearRepository;
    }

    public Report toEntity(ReportDto reportDto) {
        if (reportDto == null) {
            return null;
        } else {
            Report report = new Report();
            report.setId(reportDto.getId());
            report.setExplanation(reportDto.getExplanation());
            report.setDate(reportDto.getDate());
            report.setYear(this.getYear(reportDto.getYearName()));
            report.setReportItems(this.reportItemDtoListToReportItemList(reportDto.getReportItems()));
            this.linkReportItems(report);
            return report;
        }
    }

    public ReportDto toDto(Report report) {
        if (report == null) {
            return null;
        } else {
            ReportDto reportDto = new ReportDto();
            reportDto.setId(report.getId());
            reportDto.setExplanation(report.getExplanation());
            reportDto.setDate(report.getDate());
            reportDto.setYearName(this.getReportYearName(report));
            reportDto.setReportItems(this.reportItemListToReportItemDtoList(report.getReportItems()));
            return reportDto;
        }
    }

    public Report partialUpdate(ReportDto reportDto, Report report) {
        if (reportDto == null) {
            return null;
        } else {
            if (reportDto.getId() != null) {
                report.setId(reportDto.getId());
            }

            if (reportDto.getExplanation() != null) {
                report.setExplanation(reportDto.getExplanation());
            }

            if (reportDto.getDate() != null) {
                report.setDate(reportDto.getDate());
            }
            if (reportDto.getYearName() != null) {
                report.setYear(this.getYear(reportDto.getYearName()));
            }

            List<ReportItem> list;
            if (report.getReportItems() != null) {
                list = this.reportItemDtoListToReportItemList(reportDto.getReportItems());
                if (list != null) {
                    report.getReportItems().clear();
                    report.getReportItems().addAll(list);
                }
            } else {
                list = this.reportItemDtoListToReportItemList(reportDto.getReportItems());
                if (list != null) {
                    report.setReportItems(list);
                }
            }

            this.linkReportItems(report);
            return report;
        }
    }

    protected List<ReportItem> reportItemDtoListToReportItemList(List<ReportItemDto> list) {
        if (list == null) {
            return null;
        } else {
            List<ReportItem> list1 = new ArrayList<>(list.size());

            for (ReportItemDto reportItemDto : list) {
                list1.add(this.reportItemMapper.toEntity(reportItemDto));
            }

            return list1;
        }
    }

    protected List<ReportItemDto> reportItemListToReportItemDtoList(List<ReportItem> list) {
        if (list == null) {
            return null;
        } else {
            List<ReportItemDto> list1 = new ArrayList<>(list.size());

            for (ReportItem reportItem : list) {
                list1.add(this.reportItemMapper.toDto(reportItem));
            }

            return list1;
        }
    }

    protected Year getYear(Long yearName) {
        Optional<Year> optionalYear = yearRepository.findByYearName(yearName);
        return optionalYear.orElseThrow(() -> new EntityNotFoundException("سال با مقدار " + yearName + " یافت نشد."));
    }

    private Long getReportYearName(Report report) {
        if (report == null) {
            return null;
        } else {
            Year year = report.getYear();
            if (year == null) {
                return null;
            } else {
                return year.getName();
            }
        }
    }
}
