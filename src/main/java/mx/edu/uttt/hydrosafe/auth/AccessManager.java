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
