package org.recolnat.collection.manager.service.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.recolnat.collection.manager.repository.jpa.TaxonJPARepository;
import org.recolnat.collection.manager.service.TaxonService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaxonServiceImpl implements TaxonService {
    private final TaxonJPARepository taxonJPARepository;

    @Override
    public List<String> getFamilyListByPrefix(String query, int size) {
        if (StringUtils.isBlank(query)) {
            return Collections.emptyList();
        }
        return taxonJPARepository.findFamiliesStartingWith(query + '%', PageRequest.of(0, size));
    }

    @Override
    public List<String> getGenusListByPrefixAndFamily(String query, String family, int size) {
        if (StringUtils.isBlank(family)) {
            family = "";
        }
        if (StringUtils.isBlank(query)) {
            return Collections.emptyList();
        }
        return taxonJPARepository.findGenusStartingWithAndFilteredByFamily(query + '%', family, PageRequest.of(0, size));
    }

    @Override
    public List<String> getSpecificEpithetByPrefix(String query, String genus, Integer size) {
        if (query == null || query.trim().isEmpty()) {
            query = "";
        }
        if (StringUtils.isBlank(query)) {
            return Collections.emptyList();
        }
        return taxonJPARepository.findSpecificEpithetsStartingWithAndFilteredBy(query + '%', genus, PageRequest.of(0, size));
    }
}
