package mx.edu.uttt.hydrosafe.parametros;

import com.sun.net.httpserver.HttpExchange;
import mx.edu.uttt.hydrosafe.web.JsonHttp;

import java.io.IOException;

public class ParametroController {
    private final ParametroService service;

    public ParametroController(ParametroService service) { this.service = service; }

    public void listar(HttpExchange exchange) throws IOException {
        JsonHttp.enviar(exchange, 200, service.listar());
    }
}
