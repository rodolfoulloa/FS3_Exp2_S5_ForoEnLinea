package cl.duoc.foro.usuarios.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/** Las 6 reglas de seguridad de la contraseña. */
class ContrasenaSeguraValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {"Foro#2025", "Admin#2025", "aB3$efgh", "Clave.Segura-99"})
    void aceptaContrasenasQueCumplenTodasLasReglas(String clave) {
        assertThat(ContrasenaSeguraValidator.primeraReglaIncumplida(clave)).isEmpty();
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "Ab1#           | La contraseña debe tener entre 8 y 20 caracteres",
            "Abcdef1#Abcdef1#Abcd1 | La contraseña debe tener entre 8 y 20 caracteres",
            "clave#2025     | La contraseña debe incluir al menos una letra mayúscula",
            "CLAVE#2025     | La contraseña debe incluir al menos una letra minúscula",
            "Clave#Foro     | La contraseña debe incluir al menos un número",
            "Clave2025      | La contraseña debe incluir al menos un carácter especial (ej: ! @ # $ % & *)",
            "'Clave# 2025'  | La contraseña no puede contener espacios"
    })
    void reportaLaPrimeraReglaIncumplida(String clave, String mensaje) {
        assertThat(ContrasenaSeguraValidator.primeraReglaIncumplida(clave)).contains(mensaje);
    }
}
