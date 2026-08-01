package mx.edu.uttt.hydrosafe.web;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Router minimo hecho a mano (sin Javalin/Spring) para mantener esto "en crudo" y sin
 * dependencias pesadas. Registra rutas exactas por metodo + path y las despacha
 * desde un unico HttpHandler raiz montado en "/".
 */
public class Router {

    @FunctionalInterface
    public interface RouteHandler {
        void handle(com.sun.net.httpserver.HttpExchange exchange) throws IOException;
    }

    private record Route(String metodo, String path, RouteHandler handler) {}

    private final List<Route> rutas = new ArrayList<>();
    private RouteHandler manejadorEstatico;

    public void get(String path, RouteHandler handler) {
        rutas.add(new Route("GET", path, handler));
    }

    public void post(String path, RouteHandler handler) {
        rutas.add(new Route("POST", path, handler));
    }

    /** Se usa cuando ninguna ruta de la API coincide: sirve archivos estaticos (HTML/CSS/JS/.vue). */
    public void archivosEstaticos(RouteHandler handler) {
        this.manejadorEstatico = handler;
    }

    public void montarEn(HttpServer server) {
        server.createContext("/", exchange -> {
            try {
                String metodo = exchange.getRequestMethod();
                String path = exchange.getRequestURI().getPath();

                for (Route ruta : rutas) {
                    if (ruta.metodo().equals(metodo) && ruta.path().equals(path)) {
                        ruta.handler().handle(exchange);
                        return;
                    }
                }

                if (manejadorEstatico != null && "GET".equals(metodo)) {
                    manejadorEstatico.handle(exchange);
                    return;
                }

                JsonHttp.enviarVacio(exchange, 404);
            } catch (Exception e) {
                System.err.println("[Router] Error atendiendo " + exchange.getRequestURI() + ": " + e.getMessage());
                try {
                    JsonHttp.enviar(exchange, 500, Map.of("error", String.valueOf(e.getMessage())));
                } catch (IOException ignored) {
                    // si ni siquiera se pudo responder el error, no hay mas que hacer aqui
                }
            }
        });
    }
}
