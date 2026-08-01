package mx.edu.uttt.hydrosafe.sistema;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class EventoSistemaService {

    private final List<EventoSistema> eventos = new ArrayList<>();
    private final AtomicInteger contador = new AtomicInteger(1);

    public void registrar(String tipo, String mensaje) {
        eventos.add(new EventoSistema(contador.getAndIncrement(), tipo, mensaje, LocalDateTime.now()));
    }

    public List<EventoSistema> listar() {
        return eventos.stream().sorted(Comparator.comparing(EventoSistema::fecha).reversed()).toList();
    }
}
