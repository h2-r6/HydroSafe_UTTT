package mx.edu.uttt.hydrosafe.lecturas;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Cuerpo esperado en POST /api/lecturas. Se manda un batch de mediciones (una
 * por parametro), lo cual es mas flexible que campos fijos porque cuando el
 * admin agregue un parametro nuevo (ej. Cloro, Turbidez) el ESP32 solo tiene
 * que empezar a incluirlo en el array — el backend no cambia.
 */
public record NuevaLecturaRequest(
        List<Medicion> mediciones,
        LocalDateTime marcaTemporal
) {
    public record Medicion(int idParametro, double valorMedido) {}
}
