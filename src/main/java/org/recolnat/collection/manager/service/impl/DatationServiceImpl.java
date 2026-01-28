package org.recolnat.collection.manager.service.impl;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.recolnat.collection.manager.repository.jpa.DatationJPASpecifications.datationHasAge;
import static org.recolnat.collection.manager.repository.jpa.DatationJPASpecifications.datationHasEonothem;
import static org.recolnat.collection.manager.repository.jpa.DatationJPASpecifications.datationHasEpoch;
import static org.recolnat.collection.manager.repository.jpa.DatationJPASpecifications.datationHasEratheme;
import static org.recolnat.collection.manager.repository.jpa.DatationJPASpecifications.datationHasSystem;
import static org.springframework.data.jpa.domain.Specification.where;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.recolnat.collection.manager.repository.entity.DatationJPA;
import org.recolnat.collection.manager.repository.jpa.DatationJPARepository;
import org.recolnat.collection.manager.service.DatationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.recolnat.model.DatationResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatationServiceImpl implements DatationService {

	private final DatationJPARepository datationJPARepository;
	
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
	
	public static DatationResponseDTO buildDatationDto(List<DatationJPA> findAll) {
		Set<String> eonothemSet = new HashSet<>();
		Set<String> erathemeSet = new HashSet<>();
		Set<String> systemSet = new HashSet<>();
		Set<String> epochSet = new HashSet<>();
		Set<String> ageSet = new HashSet<>();
		findAll.stream().forEach(find -> {
				eonothemSet.add(find.getEonothem());
				erathemeSet.add(find.getEratheme());
				systemSet.add(find.getSystem());
				epochSet.add(find.getEpoch());
				ageSet.add(find.getAge());
			}
		);
		
		List<String> eonothemList = eonothemSet.stream().filter(StringUtils::isNotBlank).toList();
		List<String> erathemeList = erathemeSet.stream().filter(StringUtils::isNotBlank).toList();
		List<String> systemList = systemSet.stream().filter(StringUtils::isNotBlank).toList();
		List<String> epochList = epochSet.stream().filter(StringUtils::isNotBlank).toList();
		List<String> ageList = ageSet.stream().filter(StringUtils::isNotBlank).toList();
		
		return new DatationResponseDTO()
				.eonothem(eonothemList)
				.eratheme(erathemeList.isEmpty() ? Collections.emptyList() : erathemeList)
				.system(systemList.isEmpty() ? Collections.emptyList() : systemList)
				.epoch(epochList.isEmpty() ? Collections.emptyList() : epochList)
				.age(ageList.isEmpty() ? Collections.emptyList() : ageList);
	}

}
