package org.recolnat.collection.manager.repository.jpa;

import org.recolnat.collection.manager.repository.entity.ManagementJPA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface ManagementJPARepository extends JpaRepository<ManagementJPA, UUID> {

    @Modifying
    @Query("delete from management m where m.id = ?1")
    void removeById(UUID id);
}
