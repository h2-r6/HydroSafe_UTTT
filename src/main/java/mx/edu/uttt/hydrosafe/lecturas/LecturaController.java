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

import com.sun.net.httpserver.HttpExchange;
import mx.edu.uttt.hydrosafe.web.JsonHttp;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

public class LecturaController {
    private final LecturaService service;

    public LecturaController(LecturaService service) {
        this.service = service;
    }

    public void registrar(HttpExchange exchange) throws IOException {
        NuevaLecturaRequest req = JsonHttp.leerCuerpo(exchange, NuevaLecturaRequest.class);
        JsonHttp.enviar(exchange, 201, service.registrar(req));
    }

    public void tiempoReal(HttpExchange exchange) throws IOException {
        JsonHttp.enviar(exchange, 200, service.ultimasPorParametro());
    }

    public void historial(HttpExchange exchange) throws IOException {
        Map<String, String> query = JsonHttp.queryParams(exchange);
        Integer idParametro = query.containsKey("idParametro") ? Integer.valueOf(query.get("idParametro")) : null;
        LocalDateTime desde = query.containsKey("desde") ? LocalDateTime.parse(query.get("desde")) : null;
        LocalDateTime hasta = query.containsKey("hasta") ? LocalDateTime.parse(query.get("hasta")) : null;
        JsonHttp.enviar(exchange, 200, service.historial(idParametro, desde, hasta));
    }

    /** GET /api/lecturas/tiempo-real-min?idParametro=&minutos= -- para la grafica en vivo del dashboard. */
    public void ultimosMinutos(HttpExchange exchange) throws IOException {
        Map<String, String> query = JsonHttp.queryParams(exchange);
        int idParametro = Integer.parseInt(query.getOrDefault("idParametro", "0"));
        int minutos = Integer.parseInt(query.getOrDefault("minutos", "60"));
        JsonHttp.enviar(exchange, 200, service.ultimosMinutos(idParametro, minutos));
    }
}
