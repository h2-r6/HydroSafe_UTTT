package mx.edu.uttt.hydrosafe.alertas;

import com.sun.net.httpserver.HttpExchange;
import mx.edu.uttt.hydrosafe.web.JsonHttp;

import java.io.IOException;

public class AlertaController {
    private final AlertaService service;

    public AlertaController(AlertaService service) {
        this.service = service;
    }

    public void listar(HttpExchange exchange) throws IOException {
        JsonHttp.enviar(exchange, 200, service.listar());
    }
}
