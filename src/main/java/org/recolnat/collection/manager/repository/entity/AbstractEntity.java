package org.recolnat.collection.manager.repository.entity;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

/**
 * Abstract base class for entities. Allows parameterization of id type and implements {@link
 * #equals(Object)} and {@link #hashCode()} based on that id.
 */
@Setter
@Getter
@ToString
@MappedSuperclass
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class AbstractEntity {

    @Id
    @UuidGenerator
    @EqualsAndHashCode.Include
    private UUID id;

}
