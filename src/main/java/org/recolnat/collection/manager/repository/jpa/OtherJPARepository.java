package org.recolnat.collection.manager.repository.jpa;

import org.recolnat.collection.manager.repository.entity.OtherJPA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface OtherJPARepository extends JpaRepository<OtherJPA, UUID> {

    @Modifying
    @Query("delete from other o where o.id = ?1")
    void removeById(UUID otherJPAId);
}
