package org.recolnat.collection.manager.web;

import io.recolnat.model.SpecimenIntegrationRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.recolnat.collection.manager.common.mapper.SpecimenMapper;
import org.recolnat.collection.manager.service.MidsService;
import org.recolnat.collection.manager.web.dto.MidsDTO;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Mids")
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/mids")
public class MidsRessource {

    private final MidsService midsService;
    private final SpecimenMapper specimenMapper;

    @Operation(summary = "Récupère la valeur du MIDS d'un spécimen ainsi que les champs obligatoires pour le prochain niveau")
    @PostMapping(value = "", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<MidsDTO> getMids(
            @Parameter(name = "Spécimen à tester", required = true)
            @RequestBody SpecimenIntegrationRequestDTO dto) {

        final var specimen = specimenMapper.mapDtoToSpecimen(dto);

        MidsDTO midsDTO = midsService.processMids(specimen);
        return ResponseEntity.ok(midsDTO);
    }
}
