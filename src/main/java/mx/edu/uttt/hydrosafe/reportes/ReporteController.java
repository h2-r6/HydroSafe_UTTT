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
package mx.edu.uttt.hydrosafe.reportes;

import com.sun.net.httpserver.HttpExchange;
import mx.edu.uttt.hydrosafe.web.JsonHttp;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

public class ReporteController {
    private final ReporteService service;

    public ReporteController(ReporteService service) {
        this.service = service;
    }

    public void generar(HttpExchange exchange) throws IOException {
        Map<String, String> query = JsonHttp.queryParams(exchange);
        LocalDateTime desde = query.containsKey("desde") ? LocalDateTime.parse(query.get("desde")) : null;
        LocalDateTime hasta = query.containsKey("hasta") ? LocalDateTime.parse(query.get("hasta")) : null;
        Integer idParametro = query.containsKey("idParametro") && !query.get("idParametro").isBlank()
                ? Integer.valueOf(query.get("idParametro")) : null;
        JsonHttp.enviar(exchange, 200, service.generar(desde, hasta, idParametro));
    }
}
