package org.recolnat.collection.manager.api.domain.imports;

import io.recolnat.model.ImportCheckSpecimenResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class ImportCheckSpecimen {

    private ImportCheckSpecimenResponseDTO response;
    private Set<String> identifiers;
}
