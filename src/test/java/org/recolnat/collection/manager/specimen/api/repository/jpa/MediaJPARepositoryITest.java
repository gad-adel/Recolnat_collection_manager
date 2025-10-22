package org.recolnat.collection.manager.specimen.api.repository.jpa;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.recolnat.collection.manager.repository.entity.MediaJPA;
import org.recolnat.collection.manager.repository.jpa.MediaJPARepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Slf4j
public class MediaJPARepositoryITest {

	@Autowired
	private MediaJPARepository mediaJPARepository;
	private static final String idMedia = "a2899bb5-d6e1-4457-b414-a64619e4dd57";
	private static final String mediaName = "img.png";
	private static final String uriMediatheque = "/uri/1648799815425Or21N8YMlkAaHvwZ";

	@BeforeAll
	public static void init() {}
	
	@Test
	void add_media_should_be_ok() {
		// GIVEN
		MediaJPA media = MediaJPA.builder()
				.mediaName(mediaName)
				.mediaUrl(uriMediatheque).build();
		// WHEN
		MediaJPA save = mediaJPARepository.save(media);
		// THEN
		assertThat(save.getMediaUrl()).isEqualTo(uriMediatheque);
	}
	@Disabled(value = "no more uidMediatheque no need for deletion by this property")
	@Test
	void remove_media_should_be_ok() {
		// GIVEN
		MediaJPA media = MediaJPA.builder()
				.id(UUID.fromString(idMedia))
				.mediaName(mediaName).build();
		// WHEN
		mediaJPARepository.removeByIdMedia(UUID.fromString(idMedia));
		Optional<MediaJPA> findById = mediaJPARepository.findById(UUID.fromString(idMedia));
		
		//THEN
		assertThat(findById).isEmpty();
	}

}
