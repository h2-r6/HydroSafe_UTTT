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

import mx.edu.uttt.hydrosafe.alertas.Alerta;
import mx.edu.uttt.hydrosafe.alertas.AlertaService;
import mx.edu.uttt.hydrosafe.lecturas.Lectura;
import mx.edu.uttt.hydrosafe.lecturas.LecturaService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ReporteService {

    private final LecturaService lecturaService;
    private final AlertaService alertaService;
    private final AtomicInteger contador = new AtomicInteger(1);

    private static final int ID_ENCARGADO_UNICO = 1;

    public ReporteService(LecturaService lecturaService, AlertaService alertaService) {
        this.lecturaService = lecturaService;
        this.alertaService = alertaService;
    }

    /**
     * "En crudo": arma el resumen en JSON (Actividad 26 del PDF, regla "Generacion de
     * documentos": solo se nutre de LECTURA y ALERTA). La exportacion real a PDF/CSV
     * (entidad REPORTE, Actividad 25) se conecta despues, cuando ya haya persistencia
     * en Firebird — aqui queda el hueco listo (rutaArchivo en null).
     */
    public Map<String, Object> generar(LocalDateTime desde, LocalDateTime hasta) {
        return generar(desde, hasta, null);
    }

    public Map<String, Object> generar(LocalDateTime desde, LocalDateTime hasta, Integer idParametro) {
        List<Lectura> lecturas = lecturaService.historial(idParametro, desde, hasta);
        List<Alerta> alertas = alertaService.listar().stream()
                .filter(a -> desde == null || !a.fechaGeneracion().isBefore(desde))
                .filter(a -> hasta == null || !a.fechaGeneracion().isAfter(hasta))
                .toList();

        Reporte reporte = new Reporte(
                contador.getAndIncrement(),
                ID_ENCARGADO_UNICO,
                "JSON (borrador)",
                LocalDateTime.now(),
                null
        );

        return Map.of(
                "reporte", reporte,
                "totalLecturas", lecturas.size(),
                "totalAlertas", alertas.size(),
                "lecturas", lecturas,
                "alertas", alertas
        );
    }
}
