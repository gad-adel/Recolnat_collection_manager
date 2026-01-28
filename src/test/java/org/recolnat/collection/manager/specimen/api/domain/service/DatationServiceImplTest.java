package org.recolnat.collection.manager.specimen.api.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.recolnat.collection.manager.service.DatationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import io.recolnat.model.DatationResponseDTO;


@SpringBootTest
@ActiveProfiles("test")
class DatationServiceImplTest {

	@Autowired
	private DatationService datationServiceImpl;
	
//	@Test
//	@Sql(scripts = "classpath:init_data_datation_profile_test.sql")
//	public void retrieveDatationby_filter_fields_is_ok() {
//		// Given
//		String param1 = "Phanérozoïque";
//		String param2 = "Cénozoïque";
//		String param3 = "Quaternaire";
//		String param4 = "Pléistocène";
//		
//		// When
//		DatationResponseDTO retrieveDatation = datationServiceImpl
//					.retrieveAllDatation(param1, param2, param3, null, null);
//		
//		// Then
//		assertThat(retrieveDatation.getAge().size()).isEqualTo(5);
//	}
	
	@Test
	@Sql(scripts = "classpath:init_data_datation_profile_test.sql")
	void retrieveDatationAll_is_ok() {
		// Given
		// When
		DatationResponseDTO retrieveDatation = datationServiceImpl
					.retrieveAllDatation(null, null, null, null, null);
		
		// Then 
		assertThat(retrieveDatation.getAge()).hasSize(5);
		assertThat(retrieveDatation.getSystem().get(0)).isEqualTo("Quaternaire");
	}
}
