/**
 * HydroSafe (UTTT) — Sistema de Monitoreo de la Calidad del Agua
 * 
 * Autores:
 *   - Maria Fernanda Aldana Jiménez
 *   - Natali Isabel Chávez Alpízar
 *   - Hiyadir Raúlciel Barrera Cuervo
 * 
 * Universidad Tecnológica de Tula-Tepeji
 * Programa Educativo: Ingeniería en Tecnologías de la Información, área Infraestructura de Redes Inteligentes y Ciberseguridad
 * Empresa: Universidad Tecnológica de Tula-Tepeji
 * 
 * Asesor Académico: M. en C. Odisey Yasmin Porras Beltrán
 * Asesores Colaboradores: Marisol Reséndiz Vega, Mario Herrera Telles
 * 
 * Este software fue desarrollado durante el cuatrimestre mayo-agosto 2026.
 * Los derechos morales pertenecen a sus autores.
 * Queda prohibida la eliminación de los créditos originales y el uso o modificación del código sin autorización de los autores.
 */
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
