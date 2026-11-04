package com.essenza.aws.modules.amazonS3.domain.validation;

/**
 * Reglas para la carpeta (prefijo de clave) recibida en la subida de archivos.
 * Evita claves ambiguas o que intenten salir del prefijo esperado.
 */
public final class S3FolderValidator {

    private S3FolderValidator() {
    }

    /**
     * @throws IllegalArgumentException si la carpeta contiene {@code ..}, {@code \}, {@code //}
     *                                  o caracteres de control. Una carpeta vacía o nula es válida.
     */
    public static void validate(String folder) {
        if (folder == null || folder.isEmpty()) {
            return;
        }
        if (folder.contains("..")) {
            throw new IllegalArgumentException("carpeta no puede contener '..'");
        }
        if (folder.indexOf('\\') >= 0) {
            throw new IllegalArgumentException("carpeta no puede contener '\\'");
        }
        if (folder.contains("//")) {
            throw new IllegalArgumentException("carpeta no puede contener '//'");
        }
        if (folder.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("carpeta no puede contener caracteres de control");
        }
    }
}
