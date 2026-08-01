package mx.edu.uttt.hydrosafe.semaforo;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Guarda una "foto" del estado general (semaforo) cada vez que llega una
 * lectura nueva, para poder responder "cuantas horas estuvo en cada color
 * esta semana". Aproxima que cada snapshot representa el estado del sistema
 * hasta que llega el siguiente snapshot.
 */
public class SemaforoService {

    private final List<SnapshotSemaforo> historial = new ArrayList<>();
    private final AtomicInteger contador = new AtomicInteger(1);

    public void registrar(String estado) {
        registrar(estado, LocalDateTime.now());
    }

    public void registrar(String estado, LocalDateTime cuando) {
        historial.add(new SnapshotSemaforo(contador.getAndIncrement(), estado, cuando));
    }

    public Map<String, Double> horasUltimaSemana() {
        Map<String, Double> horas = new LinkedHashMap<>();
        horas.put("verde", 0.0);
        horas.put("amber", 0.0);
        horas.put("rojo", 0.0);

        LocalDateTime desde = LocalDateTime.now().minusDays(7);
        List<SnapshotSemaforo> ordenado = historial.stream()
                .filter(s -> !s.marcaTemporal().isBefore(desde))
                .sorted(Comparator.comparing(SnapshotSemaforo::marcaTemporal))
                .toList();

        for (int i = 0; i < ordenado.size(); i++) {
            LocalDateTime inicio = ordenado.get(i).marcaTemporal();
            LocalDateTime fin = (i + 1 < ordenado.size()) ? ordenado.get(i + 1).marcaTemporal() : LocalDateTime.now();
            double horasTramo = ChronoUnit.SECONDS.between(inicio, fin) / 3600.0;
            horas.merge(ordenado.get(i).estado(), Math.max(0, horasTramo), Double::sum);
        }
        return horas;
    }

    public List<SnapshotSemaforo> ultimos(int n) {
        return historial.stream()
                .sorted(Comparator.comparing(SnapshotSemaforo::marcaTemporal).reversed())
                .limit(n)
                .toList();
    }

    /** Genera una semana de historia sintetica para que el widget no arranque vacio. */
    public void sembrarDemo() {
        LocalDateTime cursor = LocalDateTime.now().minusDays(7);
        String[] patron = {
                "verde", "verde", "verde", "amber", "verde", "verde", "rojo",
                "amber", "verde", "verde", "verde", "amber", "verde", "verde",
                "verde", "amber", "amber", "verde", "verde", "verde", "rojo",
                "amber", "verde"
        };
        for (String estado : patron) {
            registrar(estado, cursor);
            cursor = cursor.plusHours(7);
        }
    }
}
