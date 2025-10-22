package org.recolnat.collection.manager.repository.jpa;


import org.recolnat.collection.manager.repository.entity.MailJPA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface MailJPARepository extends JpaRepository<MailJPA, UUID> {

    @Query(value = """
                select * from mail where state = 'PENDING' order by created_at limit 1 for update skip locked
            """, nativeQuery = true)
    Optional<MailJPA> findFirstPending();
}
