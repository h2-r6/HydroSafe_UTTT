package mx.edu.uttt.hydrosafe.auth;

import com.sun.net.httpserver.HttpExchange;
import mx.edu.uttt.hydrosafe.seguridad.RateLimiter;
import mx.edu.uttt.hydrosafe.sistema.EventoSistemaService;
import mx.edu.uttt.hydrosafe.usuarios.Usuario;
import mx.edu.uttt.hydrosafe.usuarios.UsuarioService;
import mx.edu.uttt.hydrosafe.web.JsonHttp;

import java.io.IOException;
import java.util.Map;

public class LoginController {

    private final UsuarioService usuarioService;
    private final RateLimiter rateLimiter = new RateLimiter();
    private final EventoSistemaService eventoSistemaService;

    public LoginController(UsuarioService usuarioService, EventoSistemaService eventoSistemaService) {
        this.usuarioService = usuarioService;
        this.eventoSistemaService = eventoSistemaService;
    }

    public record Credenciales(String correo, String contrasena) {}

    public void login(HttpExchange exchange) throws IOException {
        Credenciales creds = JsonHttp.leerCuerpo(exchange, Credenciales.class);
        String correo = creds.correo();

        long bloqueo = rateLimiter.segundosDeBloqueo(correo);
        if (bloqueo > 0) {
            JsonHttp.enviar(exchange, 429, Map.of(
                    "error", "Demasiados intentos fallidos. Intenta de nuevo en " + bloqueo + " segundos.",
                    "segundosRestantes", bloqueo));
            return;
        }

        Usuario u = correo == null || creds.contrasena() == null
                ? null : usuarioService.autenticar(correo, creds.contrasena());

        if (u == null) {
            rateLimiter.registrarFallo(correo);
            long restantes = rateLimiter.segundosDeBloqueo(correo);
            if (restantes > 0) {
                eventoSistemaService.registrar("seguridad", "Cuenta bloqueada temporalmente por intentos fallidos: " + correo);
                JsonHttp.enviar(exchange, 429, Map.of(
                        "error", "Demasiados intentos fallidos. Cuenta bloqueada " + restantes + " segundos.",
                        "segundosRestantes", restantes));
                return;
            }
            JsonHttp.enviar(exchange, 401, Map.of("error", "Correo o contrasena incorrectos"));
            return;
        }

        rateLimiter.registrarExito(correo);
        Session s = AccessManager.crearSesion(u);
        JsonHttp.enviar(exchange, 200, Map.of(
                "token", s.token,
                "correo", s.correo,
                "nombre", s.nombre,
                "rol", s.rol
        ));
    }

    public void logout(HttpExchange exchange) throws IOException {
        String header = exchange.getRequestHeaders().getFirst("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            AccessManager.cerrarSesion(header.substring("Bearer ".length()));
        }
        JsonHttp.enviarVacio(exchange, 204);
    }
}
