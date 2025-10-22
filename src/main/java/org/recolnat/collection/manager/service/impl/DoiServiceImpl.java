package org.recolnat.collection.manager.service.impl;

import lombok.RequiredArgsConstructor;
import org.recolnat.collection.manager.common.mapper.DoiMapper;
import org.recolnat.collection.manager.connector.api.DoiConnector;
import org.recolnat.collection.manager.service.DoiService;
import org.recolnat.collection.manager.web.dto.DoiDTO;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DoiServiceImpl implements DoiService {

    private final DoiMapper doiMapper;
    private final DoiConnector doiConnector;

    @Override
    public DoiDTO getDoi(String id) {
        var doi = doiConnector.getDoi(id);
        return doiMapper.toDTO(doi);
    }
}
