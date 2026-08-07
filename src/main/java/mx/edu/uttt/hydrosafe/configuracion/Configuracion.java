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
package mx.edu.uttt.hydrosafe.configuracion;

/**
 * Configuracion editable del sistema. videoUrl/githubUrl/nombreProyecto son
 * para la pagina de Documentacion (/docs), donde se puede pegar el link del
 * video demo y del repo cuando esten listos.
 */
public record Configuracion(
        String nombreApp,
        String version,
        String ubicacionNodo,
        String nodeId,
        String wifiSSID,
        int wifiRSSI,
        String microcontrolador,
        String encargadoResponsable,
        String videoUrl,
        String githubUrl
) {}
