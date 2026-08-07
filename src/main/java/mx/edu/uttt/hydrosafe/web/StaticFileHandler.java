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
package mx.edu.uttt.hydrosafe.web;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * Sirve archivos estaticos (HTML, CSS, JS, .vue) desde el classpath
 * (src/main/resources/public y src/main/resources/vue), sin depender de un framework.
 */
public class StaticFileHandler {

    private static final Map<String, String> TIPOS = Map.ofEntries(
            Map.entry("html", "text/html; charset=utf-8"),
            Map.entry("css", "text/css; charset=utf-8"),
            Map.entry("js", "application/javascript; charset=utf-8"),
            Map.entry("vue", "text/plain; charset=utf-8"),
            Map.entry("json", "application/json; charset=utf-8"),
            Map.entry("png", "image/png"),
            Map.entry("jpg", "image/jpeg"),
            Map.entry("jpeg", "image/jpeg"),
            Map.entry("svg", "image/svg+xml"),
            Map.entry("ico", "image/x-icon")
    );

    public void servir(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String recurso = resolverRecurso(path);

        InputStream in = getClass().getClassLoader().getResourceAsStream(recurso);

        if (in == null && !path.startsWith("/api/") && !path.contains(".")) {
            // Fallback tipo SPA: cualquier ruta "de app" sin extension (ej. si alguien
            // recarga en /dashboard) regresa el layout, y el router por hash
            // (#/dashboard, #/alertas...) decide la vista ya en el navegador.
            recurso = "vue/layout.html";
            in = getClass().getClassLoader().getResourceAsStream(recurso);
        }

        if (in == null) {
            JsonHttp.enviarVacio(exchange, 404);
            return;
        }

        try (InputStream is = in) {
            byte[] bytes = is.readAllBytes();
            String extension = recurso.contains(".") ? recurso.substring(recurso.lastIndexOf('.') + 1) : "html";
            exchange.getResponseHeaders().set("Content-Type", TIPOS.getOrDefault(extension, "application/octet-stream"));
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    private String resolverRecurso(String path) {
        if (path.equals("/")) return "vue/layout.html";
        return path.startsWith("/") ? path.substring(1) : path;
    }
}
