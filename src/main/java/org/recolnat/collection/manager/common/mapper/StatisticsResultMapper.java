package org.recolnat.collection.manager.common.mapper;

import org.mapstruct.Mapper;
import org.recolnat.collection.manager.api.domain.StatisticsResult;

import io.recolnat.model.StatisticsResultDTO;


@Mapper(componentModel = "spring")
public interface StatisticsResultMapper {
    StatisticsResultDTO toDto(StatisticsResult statisticsResult);
}
