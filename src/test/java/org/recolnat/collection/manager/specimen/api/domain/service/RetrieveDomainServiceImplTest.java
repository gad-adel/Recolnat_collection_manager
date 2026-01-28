package org.recolnat.collection.manager.specimen.api.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.recolnat.collection.manager.collection.api.web.AbstractResourceDBTest;
import org.recolnat.collection.manager.service.RetrieveDomainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("int")
class RetrieveDomainServiceImplTest  extends AbstractResourceDBTest {

	@Autowired
	private RetrieveDomainService retrieveDomainService;
	
	@Test
	void getAllDomain_is_ok() {
		// Given
		// When
		List<String> allDomain = retrieveDomainService.getAllDomain();
		
		// Then
		assertThat(allDomain).hasSize(4);
		
	}
}
