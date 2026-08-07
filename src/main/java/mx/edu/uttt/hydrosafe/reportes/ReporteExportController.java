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
import mx.edu.uttt.hydrosafe.lecturas.Lectura;
import mx.edu.uttt.hydrosafe.parametros.ParametroNormativo;
import mx.edu.uttt.hydrosafe.parametros.ParametroService;
import mx.edu.uttt.hydrosafe.web.CsvWriter;
import mx.edu.uttt.hydrosafe.web.JsonHttp;
import mx.edu.uttt.hydrosafe.web.MiniPdf;
import mx.edu.uttt.hydrosafe.web.MiniXlsx;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** GET /api/reportes/exportar?formato=csv|xlsx|pdf&desde=&hasta=&idParametro= */
public class ReporteExportController {

    private final ReporteService reporteService;
    private final ParametroService parametroService;

    public ReporteExportController(ReporteService reporteService, ParametroService parametroService) {
        this.reporteService = reporteService;
        this.parametroService = parametroService;
    }

    @SuppressWarnings("unchecked")
    public void exportar(HttpExchange e) throws IOException {
        Map<String, String> query = JsonHttp.queryParams(e);
        LocalDateTime desde = query.containsKey("desde") ? LocalDateTime.parse(query.get("desde")) : null;
        LocalDateTime hasta = query.containsKey("hasta") ? LocalDateTime.parse(query.get("hasta")) : null;
        Integer idParametro = query.containsKey("idParametro") && !query.get("idParametro").isBlank()
                ? Integer.valueOf(query.get("idParametro")) : null;
        String formato = query.getOrDefault("formato", "csv").toLowerCase();

        Map<String, Object> reporte = reporteService.generar(desde, hasta, idParametro);
        List<Lectura> lecturas = (List<Lectura>) reporte.get("lecturas");
        List<ParametroNormativo> parametros = parametroService.listar();

        switch (formato) {
            case "xlsx" -> exportarXlsx(e, lecturas, parametros);
            case "pdf" -> exportarPdf(e, lecturas, reporte);
            default -> exportarCsv(e, lecturas, parametros);
        }
    }

    private void exportarCsv(HttpExchange e, List<Lectura> lecturas, List<ParametroNormativo> parametros) throws IOException {
        byte[] datos = CsvWriter.deLecturas(lecturas, parametros);
        responder(e, datos, "text/csv; charset=utf-8", "reporte_hydrosafe.csv");
    }

    private void exportarXlsx(HttpExchange e, List<Lectura> lecturas, List<ParametroNormativo> parametros) throws IOException {
        Map<Integer, String> nombres = nombresPorId(parametros);
        List<String> encabezados = List.of("Fecha", "Parametro", "Valor");
        List<List<Object>> filas = new ArrayList<>();
        for (Lectura l : lecturas) {
            filas.add(List.of(l.marcaTemporal().toString(), nombres.getOrDefault(l.idParametro(), "Parametro " + l.idParametro()), l.valorMedido()));
        }
        byte[] datos = MiniXlsx.generar("Reporte HydroSafe", encabezados, filas);
        responder(e, datos, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "reporte_hydrosafe.xlsx");
    }

    private void exportarPdf(HttpExchange e, List<Lectura> lecturas, Map<String, Object> reporte) throws IOException {
        Map<Integer, String> nombres = nombresPorId(parametroService.listar());

        MiniPdf pdf = new MiniPdf();
        pdf.titulo("Reporte de Calidad del Agua - HydroSafe UTTT")
           .subtitulo("Generado: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
           .subtitulo("Total de mediciones: " + lecturas.size() + "   |   Total de alertas: " + reporte.get("totalAlertas"))
           .espacio(10)
           .linea()
           .espacio(6);

        List<String> encabezados = List.of("Fecha", "Parametro", "Valor");
        List<List<String>> filas = new ArrayList<>();
        for (Lectura l : lecturas) {
            filas.add(List.of(
                    l.marcaTemporal().toString().replace("T", " "),
                    nombres.getOrDefault(l.idParametro(), "Parametro " + l.idParametro()),
                    String.valueOf(l.valorMedido())
            ));
        }
        pdf.tabla(encabezados, filas);

        byte[] datos = pdf.generar();
        responder(e, datos, "application/pdf", "reporte_hydrosafe.pdf");
    }

    private Map<Integer, String> nombresPorId(List<ParametroNormativo> parametros) {
        Map<Integer, String> nombres = new HashMap<>();
        for (ParametroNormativo p : parametros) nombres.put(p.idParametro(), p.nombreParametro() + " (" + p.unidadMedida() + ")");
        return nombres;
    }

    private void responder(HttpExchange e, byte[] datos, String contentType, String nombreArchivo) throws IOException {
        e.getResponseHeaders().set("Content-Type", contentType);
        e.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"" + nombreArchivo + "\"");
        e.sendResponseHeaders(200, datos.length);
        try (OutputStream os = e.getResponseBody()) { os.write(datos); }
    }
}
