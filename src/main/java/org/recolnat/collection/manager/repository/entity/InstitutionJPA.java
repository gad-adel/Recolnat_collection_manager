package org.recolnat.collection.manager.repository.entity;

import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.Hibernate;
import org.recolnat.collection.manager.api.domain.enums.PartnerType;
import org.recolnat.collection.manager.api.domain.enums.PartnerTypeConverter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity(name = "institution")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
@SuperBuilder
public class InstitutionJPA {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private UUID institutionId;
    private String code;
    private String name;
    private String mandatoryDescription;
    private String optionalDescription;
    @Convert(converter = PartnerTypeConverter.class)
    private PartnerType partnerType;
    private String logoUrl;
    private LocalDateTime createdAt;
    private String createdBy;
    private String modifiedBy;
    private LocalDateTime modifiedAt;
    private LocalDateTime dataChangeTs;

    private String url;
    private Integer specimensCount;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "institution")
    @ToString.Exclude
    private List<CollectionJPA> collections;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        InstitutionJPA that = (InstitutionJPA) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
