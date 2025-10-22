package org.recolnat.collection.manager.connector.api.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DoiCreated {
    @JsonProperty("date-parts")
    private List<List<Integer>> parts;
}
