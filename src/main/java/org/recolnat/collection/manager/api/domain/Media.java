package org.recolnat.collection.manager.api.domain;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@SuperBuilder
@EqualsAndHashCode
public class Media extends DomainId {

    private String creator;
    private String contributor;
    private String license;
    private String source;
    private String description;
    private String mediaUrl;
    @NotEmpty(message = "mediaName is required")
    private String mediaName;
    private Boolean isCover;

}
