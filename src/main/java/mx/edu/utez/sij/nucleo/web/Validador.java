package mx.edu.utez.sij.nucleo.web;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Revisa las anotaciones de Jakarta Validation (@NotBlank, @Email, @Size, @Pattern...) de un formulario.
 *
 * <pre>
 * Map&lt;String, String&gt; errores = Validador.validar(form);   // campo → mensaje
 * if (!errores.isEmpty()) {
 *     request.setAttribute("errores", errores);
 *     ... volver a mostrar el formulario ...
 * }
 * </pre>
 *
 * En la JSP: ${errores.nombre} o &lt;n:error campo="nombre"/&gt;.
 * Si un campo tiene varios errores, se queda el primero en orden alfabético del mensaje (siempre el mismo).
 */
public final class Validador {

    // Se crea una sola vez: es costoso. ParameterMessageInterpolator evita depender de una librería de EL.
    private static final ValidatorFactory FABRICA = Validation.byDefaultProvider()
            .configure()
            .messageInterpolator(new ParameterMessageInterpolator())
            .buildValidatorFactory();

    private static final Validator VALIDADOR = FABRICA.getValidator();

    private Validador() {
    }

    public static Map<String, String> validar(Object formulario) {
        Map<String, String> errores = new LinkedHashMap<>();
        VALIDADOR.validate(formulario).stream()
                .sorted(Comparator.comparing((ConstraintViolation<Object> v) -> v.getPropertyPath().toString())
                        .thenComparing(ConstraintViolation::getMessage))
                .forEach(v -> errores.putIfAbsent(v.getPropertyPath().toString(), v.getMessage()));
        return errores;
    }
}
