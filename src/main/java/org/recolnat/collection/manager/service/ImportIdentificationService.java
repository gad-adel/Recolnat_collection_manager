package org.recolnat.collection.manager.service;

import io.recolnat.model.ImportCheckDataResponseDTO;
import io.recolnat.model.TemplateStructureDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface ImportIdentificationService {

    ImportCheckDataResponseDTO checkDeterminations(MultipartFile file, UUID institutionId);

    String generateIdentificationTemplate(List<String> columns) throws IOException;

    List<TemplateStructureDTO> getTemplateStructure();

}
