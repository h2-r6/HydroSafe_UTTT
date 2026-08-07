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
package mx.edu.uttt.hydrosafe.documentos;

import com.sun.net.httpserver.HttpExchange;
import mx.edu.uttt.hydrosafe.web.JsonHttp;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class DocumentoController {

    private final DocumentoService service;

    public DocumentoController(DocumentoService service) { this.service = service; }

    /** GET /api/documentos/presentacion -- info (nombre, fecha), no el archivo. */
    public void info(HttpExchange e) throws IOException {
        JsonHttp.enviar(e, 200, service.info());
    }

    /** POST /api/admin/documentos/presentacion?nombre=archivo.pptx -- sube el archivo en crudo (bytes del body). */
    public void subir(HttpExchange e) throws IOException {
        var query = JsonHttp.queryParams(e);
        String nombre = query.containsKey("nombre") ? URLDecoder.decode(query.get("nombre"), StandardCharsets.UTF_8) : "presentacion";
        byte[] datos = e.getRequestBody().readAllBytes();
        if (datos.length == 0) {
            JsonHttp.enviar(e, 400, java.util.Map.of("error", "Archivo vacio"));
            return;
        }
        if (datos.length > 50 * 1024 * 1024) {
            JsonHttp.enviar(e, 413, java.util.Map.of("error", "Archivo demasiado grande (max 50MB)"));
            return;
        }
        service.guardar(nombre, datos);
        JsonHttp.enviar(e, 200, service.info());
    }

    /** GET /api/documentos/presentacion/descargar -- el archivo binario en si. */
    public void descargar(HttpExchange e) throws IOException {
        byte[] datos = service.leer();
        if (datos == null) { JsonHttp.enviarVacio(e, 404); return; }
        String nombre = service.nombreOriginal() != null ? service.nombreOriginal() : "presentacion";
        e.getResponseHeaders().set("Content-Type", "application/octet-stream");
        e.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"" + nombre + "\"");
        e.sendResponseHeaders(200, datos.length);
        try (OutputStream os = e.getResponseBody()) { os.write(datos); }
    }
}
