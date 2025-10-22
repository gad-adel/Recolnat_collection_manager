package org.recolnat.collection.manager.service.impl;

import io.recolnat.model.DatationResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.recolnat.collection.manager.repository.entity.DatationJPA;
import org.recolnat.collection.manager.repository.jpa.DatationJPARepository;
import org.recolnat.collection.manager.service.DatationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.recolnat.collection.manager.repository.jpa.DatationJPASpecifications.datationHasAge;
import static org.recolnat.collection.manager.repository.jpa.DatationJPASpecifications.datationHasEonothem;
import static org.recolnat.collection.manager.repository.jpa.DatationJPASpecifications.datationHasEpoch;
import static org.recolnat.collection.manager.repository.jpa.DatationJPASpecifications.datationHasEratheme;
import static org.recolnat.collection.manager.repository.jpa.DatationJPASpecifications.datationHasSystem;
import static org.springframework.data.jpa.domain.Specification.where;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatationServiceImpl implements DatationService {

    private final DatationJPARepository datationJPARepository;

    public static DatationResponseDTO buildDatationDto(List<DatationJPA> findAll) {
        Set<String> eonothemSet = new HashSet<>();
        Set<String> erathemeSet = new HashSet<>();
        Set<String> systemSet = new HashSet<>();
        Set<String> epochSet = new HashSet<>();
        Set<String> ageSet = new HashSet<>();
        findAll.forEach(find -> {
                    eonothemSet.add(find.getEonothem());
                    erathemeSet.add(find.getEratheme());
                    systemSet.add(find.getSystem());
                    epochSet.add(find.getEpoch());
                    ageSet.add(find.getAge());
                }
        );

        List<String> eonothemList = eonothemSet.stream().filter(StringUtils::isNotBlank).sorted().toList();
        List<String> erathemeList = erathemeSet.stream().filter(StringUtils::isNotBlank).sorted().toList();
        List<String> systemList = systemSet.stream().filter(StringUtils::isNotBlank).sorted().toList();
        List<String> epochList = epochSet.stream().filter(StringUtils::isNotBlank).sorted().toList();
        List<String> ageList = ageSet.stream().filter(StringUtils::isNotBlank).sorted().toList();

        return new DatationResponseDTO()
                .eonothem(eonothemList)
                .eratheme(erathemeList)
                .system(systemList)
                .epoch(epochList)
                .age(ageList);
    }

    @Override
    @Transactional(readOnly = true)
    public DatationResponseDTO retrieveAllDatation(String eonothem, String eratheme, String system, String epoch, String age) {
        List<DatationJPA> findAll = datationJPARepository.findAll(
                where(isBlank(eonothem) ? null : datationHasEonothem(eonothem))
                        .and(isBlank(eratheme) ? null : datationHasEratheme(eratheme))
                        .and(isBlank(system) ? null : datationHasSystem(system))
                        .and(isBlank(epoch) ? null : datationHasEpoch(epoch))
                        .and(isBlank(age) ? null : datationHasAge(age))
        );

        return buildDatationDto(findAll);
    }

}
