package mx.edu.uttt.hydrosafe.semaforo;

import com.sun.net.httpserver.HttpExchange;
import mx.edu.uttt.hydrosafe.web.JsonHttp;

import java.io.IOException;
import java.util.Map;

public class SemaforoController {
    private final SemaforoService service;

    public SemaforoController(SemaforoService service) { this.service = service; }

    public void horasSemana(HttpExchange e) throws IOException {
        JsonHttp.enviar(e, 200, Map.of(
                "horas", service.horasUltimaSemana(),
                "snapshots", service.ultimos(40)
        ));
    }
}
