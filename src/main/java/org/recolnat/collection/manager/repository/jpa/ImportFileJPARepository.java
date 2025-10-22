package org.recolnat.collection.manager.repository.jpa;

import org.recolnat.collection.manager.repository.entity.ImportFileJPA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ImportFileJPARepository extends JpaRepository<ImportFileJPA, UUID> {

    List<ImportFileJPA> findAllByImportJPA_Id(UUID importJPAId);

    @Query(value = """
            select f from importFile f join fetch f.importJPA where f.id = :fileId
            """)
    Optional<ImportFileJPA> findByIdFetchImport(UUID fileId);
}
