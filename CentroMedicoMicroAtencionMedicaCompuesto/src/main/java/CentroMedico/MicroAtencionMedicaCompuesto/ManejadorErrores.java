package CentroMedico.MicroAtencionMedicaCompuesto;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;

// Traduce los errores de los micros que este compuesto consume a respuestas claras.
@RestControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(HttpStatusCodeException.class)
    public ResponseEntity<Map<String, String>> remoto(HttpStatusCodeException ex) {
        String msg = ex.getStatusText();
        try {
            Map<?, ?> cuerpo = ex.getResponseBodyAs(Map.class);
            if (cuerpo != null && cuerpo.get("message") != null) {
                msg = String.valueOf(cuerpo.get("message"));
            }
        } catch (RuntimeException ignorada) {
            // cuerpo ilegible: se usa el texto del estado
        }
        return ResponseEntity.status(ex.getStatusCode()).body(Map.of("message", msg));
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<Map<String, String>> caido(ResourceAccessException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("message", "Un microservicio no responde. Verifique que esté registrado en Eureka."));
    }
}