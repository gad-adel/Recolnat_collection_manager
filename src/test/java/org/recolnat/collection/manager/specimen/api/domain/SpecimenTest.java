package org.recolnat.collection.manager.specimen.api.domain;

import org.junit.jupiter.api.Test;
import org.recolnat.collection.manager.api.domain.Specimen;

import static org.assertj.core.api.Assertions.assertThat;

class SpecimenTest {

    @Test
    void isDraftValid_should_be_ok() {
        var spec = Specimen.builder().catalogNumber("1234").build();
        assertThat(spec.isDraftValid()).isTrue();
    }

    @Test
    void isDraftValid_should_be_ko() {
        var spec = Specimen.builder().build();
        assertThat(spec.isDraftValid()).isFalse();
    }

}
