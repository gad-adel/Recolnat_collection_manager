package org.recolnat.collection.manager.specimen.api.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.recolnat.collection.manager.collection.api.web.AbstractResourceDBTest;
import org.recolnat.collection.manager.service.RetrievePlaceholderDatationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("int")
public class RetrievePlaceholderDatationServiceImplITest  extends AbstractResourceDBTest {

	@Autowired
	private RetrievePlaceholderDatationService retrievePlaceholderDatationService;
	
	@Test
	void getPlaceholderDatation_is_ok() {
		// Given
		// When
		List<String> placeholderDatation = retrievePlaceholderDatationService.getPlaceholderDatation();
		
		// Then
		assertThat(placeholderDatation).hasSize(10);
	}
}
