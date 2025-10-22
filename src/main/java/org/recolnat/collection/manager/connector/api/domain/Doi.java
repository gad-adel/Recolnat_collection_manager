package org.recolnat.collection.manager.connector.api.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Doi {

    private String title;
    private List<DoiAuthor> author;
    private DoiCreated published;

}
