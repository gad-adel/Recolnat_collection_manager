package org.recolnat.collection.manager.repository.jpa;

import org.recolnat.collection.manager.repository.entity.MediaJPA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface MediaJPARepository extends JpaRepository<MediaJPA, UUID> {
    @Modifying
    @Query("DELETE MediaJPA m WHERE m.id = ?1")
    void removeByIdMedia(UUID uid);
}
