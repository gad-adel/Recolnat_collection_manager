package org.recolnat.collection.manager.api.domain.enums;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.recolnat.collection.manager.common.exception.CollectionManagerBusinessException;
import org.recolnat.collection.manager.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.Optional;


@Getter
public enum PartnerType {

    PARTNER("Partner", "Partenaire"),
    DATA_PROVIDER("Data provider", "Fournisseur de données"),
    MEMBER("Member", "Membre");

    private final String partnerEn;
    private final String partnerFr;

    PartnerType(String partnerEn, String partnerFr) {
        this.partnerEn = partnerEn;
        this.partnerFr = partnerFr;
    }

    public static PartnerType getpartnerType(@NotNull String label) {
        Optional<PartnerType> partner = Arrays.asList(PartnerType.values()).parallelStream().
                filter(p -> p.partnerEn.equalsIgnoreCase(label)
                            || p.partnerFr.equalsIgnoreCase(label)
                            || p.name().equalsIgnoreCase(label)).findFirst();

        if (partner.isPresent()) {
            return partner.get();
        } else {
            throw new CollectionManagerBusinessException(HttpStatus.NOT_FOUND, ErrorCode.ERR_NFE_CODE, "label partner not found: " + label);
        }

    }
}
