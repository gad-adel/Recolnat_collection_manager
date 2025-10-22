package org.recolnat.collection.manager.connector.api.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MediathequeOutput {

	@JsonProperty("success")
	private Integer success;

	@JsonProperty("media")
	private MediaDetailsOutput media;

	@JsonProperty("errors")
	@Valid
	private List<MediaErrorsOutput> errors = null;
}
