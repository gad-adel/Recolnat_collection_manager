package org.recolnat.collection.manager.api.domain;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
@Builder
public class Institution {
    private Integer id;
    @NotBlank(message = "Code institution may not be empty")
    private String code;
    @NotBlank(message = "Name institution may not be empty")
    private String name;
    @NotBlank(message = "Mandatory description may not be empty")
    private String mandatoryDescription;
    private String optionalDescription;
    @NotBlank(message = "partner type may not be empty")
    private String partnerType;
    private String partnerTypeEn;
    private String partnerTypeFr;
    private String logoUrl;
    private List<CollectionsInstitution> collections;
    private Boolean assignable;
    private LocalDateTime createdAt;
    private String createdBy;
    private String modifiedBy;
    private LocalDateTime modifiedAt;
    private UUID institutionId;
    private String url;
    private Integer specimensCount;

}
