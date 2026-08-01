package mx.edu.uttt.hydrosafe.eventos;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public class EventosController {
    private final EventBus bus;

    public EventosController(EventBus bus) { this.bus = bus; }

    /** GET /api/eventos -- deja la conexion abierta (SSE) hasta que el cliente se desconecte. */
    public void suscribir(HttpExchange exchange) throws IOException {
        bus.atender(exchange);
    }
}
