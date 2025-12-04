package org.recolnat.collection.manager.service;

import io.recolnat.model.ImportCheckDataResponseDTO;
import io.recolnat.model.TemplateStructureDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface ImportSpecimenService {

    ImportCheckDataResponseDTO checkSpecimens(MultipartFile file, UUID institutionId);

    String generateSpecimenTemplate(List<String> columns);

    List<TemplateStructureDTO> getTemplateStructure();

}
