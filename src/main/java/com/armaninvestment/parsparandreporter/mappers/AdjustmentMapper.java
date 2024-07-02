package com.armaninvestment.parsparandreporter.mappers;

import com.armaninvestment.parsparandreporter.dtos.AdjustmentDto;
import com.armaninvestment.parsparandreporter.entities.Adjustment;


public interface AdjustmentMapper {
    Adjustment toEntity(AdjustmentDto adjustmentDto);

    AdjustmentDto toDto(Adjustment adjustment);

    Adjustment partialUpdate(AdjustmentDto adjustmentDto, Adjustment adjustment);
}