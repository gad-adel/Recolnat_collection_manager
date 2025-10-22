package org.recolnat.collection.manager.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface MediaIntegrationService {

    List<String> add(UUID specimenId, List<MultipartFile> fileName);

    List<String> addDraft(UUID specimenId, List<MultipartFile> fileName);

    List<String> addReviewed(UUID specimenId, List<MultipartFile> fileName);
}
