package mx.edu.uttt.hydrosafe.configuracion;

import com.sun.net.httpserver.HttpExchange;
import mx.edu.uttt.hydrosafe.web.JsonHttp;

import java.io.IOException;

public class ConfiguracionController {
    private final ConfiguracionService service;
    public ConfiguracionController(ConfiguracionService service) { this.service = service; }

    public void obtener(HttpExchange e) throws IOException {
        JsonHttp.enviar(e, 200, service.obtener());
    }
}
