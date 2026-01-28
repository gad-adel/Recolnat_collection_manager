package org.recolnat.collection.manager.api.domain;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = false)
@SuperBuilder
@Slf4j
public class Specimen extends DomainId {

    private UUID collectionId;
    private String collectionName;
    private String collectionCode;
    private String institutionId;
    private String catalogNumber;
    private String recordNumber;
    private String basisOfRecord;
    private String preparations;
    private String individualCount;
    private String sex;
    private String lifeStage;
    private String occurrenceRemarks;
    private String legalStatus;
    private String donor;
    private LocalDateTime createdAt;
    private String createdBy;
    private String modifiedBy;
    private LocalDateTime modifiedAt;
    private String state;
    private GeologicalContext geologicalContext;
    private CollectionEvent collectionEvent;
    private Set<Literature> literatures = new HashSet<>();
    private String nominativeCollection;
    @NotEmpty(groups = NormalCheck.class, message = " at least one identification is required")
    @Size(min = 1, groups = NormalCheck.class, message = " at least one identification is required")
    private Set<@Valid Identification> identifications = new HashSet<>();
    private List<Media> medias = new ArrayList<>();
    private Other other;
    private Management management;

    public boolean isDraftValid() {
        Class<? extends Specimen> specimenClass = this.getClass();
        Field[] fields = specimenClass.getDeclaredFields();
        return Arrays.stream(fields).anyMatch(field -> {
            try {
                return field.get(this) != null && !field.getType().equals(org.slf4j.Logger.class);
            } catch (IllegalAccessException e) {
                log.error(e.getMessage(), e);
                return false;
            }
        });
    }

}
