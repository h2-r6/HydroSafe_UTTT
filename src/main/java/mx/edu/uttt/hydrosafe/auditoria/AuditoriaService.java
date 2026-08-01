package mx.edu.uttt.hydrosafe.auditoria;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AuditoriaService {

    private final List<EventoAuditoria> eventos = new ArrayList<>();
    private final AtomicInteger contador = new AtomicInteger(1);

    public void registrar(String usuario, String accion, String detalle) {
        eventos.add(new EventoAuditoria(contador.getAndIncrement(), usuario, accion, detalle, LocalDateTime.now()));
    }

    public List<EventoAuditoria> listar() {
        return eventos.stream().sorted(Comparator.comparing(EventoAuditoria::fecha).reversed()).toList();
    }
}
