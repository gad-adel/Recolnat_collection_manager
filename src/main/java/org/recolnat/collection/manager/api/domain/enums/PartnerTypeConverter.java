package org.recolnat.collection.manager.api.domain.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import static org.apache.commons.lang3.StringUtils.isEmpty;


/**
 *The only reason for the presence of the converter is the fact that some institutions do not have a PartnerType defined in the database (null field)
 */
@Converter(autoApply = true)
public class PartnerTypeConverter implements AttributeConverter<PartnerType, String> {


    @Override
    public PartnerType convertToEntityAttribute(String dbData) {
        if(isEmpty(dbData)) {
            return null;
        }
        return PartnerType.valueOf(dbData);
    }

	@Override
	public String convertToDatabaseColumn(PartnerType attribute) {
		return attribute.name();
	}

}