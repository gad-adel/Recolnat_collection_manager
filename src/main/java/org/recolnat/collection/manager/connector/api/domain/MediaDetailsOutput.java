package org.recolnat.collection.manager.connector.api.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MediaDetailsOutput {

	@JsonProperty("uid")
	private String uid;

	@JsonProperty("url")
	private String url;

	@JsonProperty("mimeType")
	private String mimeType;
}
