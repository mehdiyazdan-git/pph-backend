package com.armaninvestment.parsparandreporter.dtos;

import com.armaninvestment.parsparandreporter.entities.AppSetting;
import com.armaninvestment.parsparandreporter.entities.AppSettingDto;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface AppSettingMapper {
    AppSetting toEntity(AppSettingDto appSettingDto);

    AppSettingDto toDto(AppSetting appSetting);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    AppSetting partialUpdate(AppSettingDto appSettingDto, @MappingTarget AppSetting appSetting);
}