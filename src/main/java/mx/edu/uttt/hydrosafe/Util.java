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
package mx.edu.uttt.hydrosafe;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Utilidades compartidas. Punto de partida minimo, ve agregando lo que necesites aqui. */
public class Util {

    private static final DateTimeFormatter FORMATO_LEGIBLE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public static String ahora() {
        return LocalDateTime.now().format(FORMATO_LEGIBLE);
    }
}
