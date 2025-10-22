package org.recolnat.collection.manager.repository.jpa;


import org.recolnat.collection.manager.repository.entity.IdentificationJPA;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IdentificationJPARepository extends JpaRepository<IdentificationJPA, UUID> {

}
