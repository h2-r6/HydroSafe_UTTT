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
package mx.edu.uttt.hydrosafe.auth;

import com.sun.net.httpserver.HttpExchange;
import mx.edu.uttt.hydrosafe.usuarios.Usuario;
import mx.edu.uttt.hydrosafe.web.JsonHttp;
import mx.edu.uttt.hydrosafe.web.Router;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AccessManager {

    private static final Map<String, Session> sesiones = new ConcurrentHashMap<>();

    public static Session crearSesion(Usuario u) {
        String token = UUID.randomUUID().toString();
        Session s = new Session(token, u.correo(), u.nombre(), u.rol());
        sesiones.put(token, s);
        return s;
    }

    public static Session obtenerSesion(String token) {
        return token != null ? sesiones.get(token) : null;
    }

    public static void cerrarSesion(String token) {
        if (token != null) sesiones.remove(token);
    }

    public static Session sesionDesde(HttpExchange exchange) {
        String header = exchange.getRequestHeaders().getFirst("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return obtenerSesion(header.substring("Bearer ".length()));
        }
        // EventSource (SSE) no puede mandar headers personalizados desde el navegador,
        // asi que /api/eventos manda el token como query string en su lugar.
        String token = mx.edu.uttt.hydrosafe.web.JsonHttp.queryParams(exchange).get("token");
        return obtenerSesion(token);
    }

    /** Cualquier usuario con sesion valida (monitor o admin). */
    public static Router.RouteHandler proteger(Router.RouteHandler handler) {
        return (HttpExchange exchange) -> {
            if (sesionDesde(exchange) == null) {
                JsonHttp.enviar(exchange, 401, Map.of("error", "Se requiere iniciar sesion"));
                return;
            }
            handler.handle(exchange);
        };
    }

    /** Solo administradores. */
    public static Router.RouteHandler protegerAdmin(Router.RouteHandler handler) {
        return (HttpExchange exchange) -> {
            Session s = sesionDesde(exchange);
            if (s == null) {
                JsonHttp.enviar(exchange, 401, Map.of("error", "Se requiere iniciar sesion"));
                return;
            }
            if (!"admin".equals(s.rol)) {
                JsonHttp.enviar(exchange, 403, Map.of("error", "Solo administradores"));
                return;
            }
            handler.handle(exchange);
        };
    }
}
