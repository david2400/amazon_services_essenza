package com.essenza.aws.modules.amazonS3.domain.validation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class S3FolderValidatorTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"email/imagenes/2026/11", "email/imagenes/2026/11/", "documentos", "a-b_c.d/e"})
    void aceptaCarpetasValidas(String folder) {
        assertThatCode(() -> S3FolderValidator.validate(folder)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "../secretos", "email/imagenes/../../otros", "email/..", "email\\imagenes",
            "email//imagenes", "//email", "email/imagenes\n2026", "email/\u0000/a", "email/\t/a", "email/\u007F"
    })
    void rechazaCarpetasPeligrosas(String folder) {
        assertThatThrownBy(() -> S3FolderValidator.validate(folder))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageStartingWith("carpeta no puede contener");
    }
}
