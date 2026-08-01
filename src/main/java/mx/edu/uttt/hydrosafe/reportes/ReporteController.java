package mx.edu.uttt.hydrosafe.reportes;

import com.sun.net.httpserver.HttpExchange;
import mx.edu.uttt.hydrosafe.web.JsonHttp;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

public class ReporteController {
    private final ReporteService service;

    public ReporteController(ReporteService service) {
        this.service = service;
    }

    public void generar(HttpExchange exchange) throws IOException {
        Map<String, String> query = JsonHttp.queryParams(exchange);
        LocalDateTime desde = query.containsKey("desde") ? LocalDateTime.parse(query.get("desde")) : null;
        LocalDateTime hasta = query.containsKey("hasta") ? LocalDateTime.parse(query.get("hasta")) : null;
        Integer idParametro = query.containsKey("idParametro") && !query.get("idParametro").isBlank()
                ? Integer.valueOf(query.get("idParametro")) : null;
        JsonHttp.enviar(exchange, 200, service.generar(desde, hasta, idParametro));
    }
}
