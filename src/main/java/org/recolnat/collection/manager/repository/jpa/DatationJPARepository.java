package org.recolnat.collection.manager.repository.jpa;

import org.recolnat.collection.manager.repository.entity.DatationJPA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DatationJPARepository extends JpaRepository<DatationJPA, Integer>, JpaSpecificationExecutor<DatationJPA>{

}
