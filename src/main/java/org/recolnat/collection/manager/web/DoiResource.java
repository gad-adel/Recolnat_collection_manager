package org.recolnat.collection.manager.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.recolnat.collection.manager.service.DoiService;
import org.recolnat.collection.manager.web.dto.DoiDTO;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DoiResource {

    private final DoiService doiService;

    @Operation(summary = "Récupère la valeur d'un DTO en fonction de son identifiant")
    @GetMapping(value = "/v1/doi", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<DoiDTO> getDoi(
            @NotNull @Parameter(name = "id", description = "Identifiant du DOI", required = true, in = ParameterIn.QUERY) @Valid @RequestParam(value = "id", required = true) String id) {
        var result = doiService.getDoi(id);
        return ResponseEntity.ok(result);
    }

}
