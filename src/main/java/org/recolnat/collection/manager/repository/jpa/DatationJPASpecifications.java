package org.recolnat.collection.manager.repository.jpa;

import org.recolnat.collection.manager.repository.entity.DatationJPA;
import org.springframework.data.jpa.domain.Specification;

public class DatationJPASpecifications {
	
	private DatationJPASpecifications( ) {}

	public static Specification<DatationJPA> datationHasEonothem(String eonothem) {
		return (root, query, cb) -> cb.equal(root.get("eonothem"), eonothem);
	}
	
	public static Specification<DatationJPA> datationHasEratheme(String eratheme) {
		return (root, query, cb) -> cb.equal(root.get("eratheme"), eratheme);
	}
	
	public static Specification<DatationJPA> datationHasSystem(String system) {
		return (root, query, cb) -> cb.equal(root.get("system"), system);
	}
	
	public static Specification<DatationJPA> datationHasEpoch(String epoch) {
		return (root, query, cb) -> cb.equal(root.get("epoch"), epoch);
	}
	
	public static Specification<DatationJPA> datationHasAge(String age) {
		return (root, query, cb) -> cb.equal(root.get("age"), age);
	}
	
}
