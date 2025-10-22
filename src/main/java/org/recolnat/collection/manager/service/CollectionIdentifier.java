package org.recolnat.collection.manager.service;

import lombok.*;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
@Builder
public class
CollectionIdentifier {
    private UUID specimenId;
    private UUID collectionId;

}
