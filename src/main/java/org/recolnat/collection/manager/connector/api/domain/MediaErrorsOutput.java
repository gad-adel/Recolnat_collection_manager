package org.recolnat.collection.manager.connector.api.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MediaErrorsOutput {

	@JsonProperty("source")
	private String source;

	@JsonProperty("error")
	private String error;
}
