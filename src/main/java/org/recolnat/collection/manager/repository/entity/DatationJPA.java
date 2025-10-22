package org.recolnat.collection.manager.repository.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@Entity(name =  "datation")
public class DatationJPA {
	@Id
	private Integer id;
	private String eonothem;
	private String eratheme;
	private String system;
	private String epoch;
	private String age;
	private String createdBy;
	private LocalDateTime createdAt;
	private String modifiedBy;
	private LocalDateTime modifiedAt;
}
