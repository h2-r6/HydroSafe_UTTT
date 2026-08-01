package mx.edu.uttt.hydrosafe.alertas;

import mx.edu.uttt.hydrosafe.lecturas.Lectura;
import mx.edu.uttt.hydrosafe.parametros.ParametroNormativo;
import mx.edu.uttt.hydrosafe.parametros.ParametroService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AlertaService {

    private final List<Alerta> alertas = new ArrayList<>();
    private final AtomicInteger contador = new AtomicInteger(1);
    private final ParametroService parametroService;

    public AlertaService(ParametroService parametroService) {
        this.parametroService = parametroService;
    }

    /**
     * Regla del PDF (Actividad 26): dispara alerta si la lectura se sale del
     * rango normativo. Para "presencia" (E. coli) cualquier valor > 0 dispara
     * una alerta critica que exige confirmacion microbiologica.
     */
    public Alerta evaluar(Lectura lectura) {
        ParametroNormativo p = parametroService.porId(lectura.idParametro());
        if (p == null) return null;

        String prioridad;
        String descripcion;

        if ("presencia".equals(p.tipo())) {
            if (lectura.valorMedido() <= 0) return null;
            prioridad = "Critica";
            descripcion = "Posible presencia de " + p.nombreParametro().replace("Posible ", "")
                    + " detectada mediante analisis de patrones. Se recomienda realizar una prueba "
                    + "microbiologica para confirmar el resultado.";
        } else {
            boolean fueraDeRango = lectura.valorMedido() < p.limiteMin() || lectura.valorMedido() > p.limiteMax();
            if (!fueraDeRango) {
                // Zona de advertencia: dentro del rango pero al 85% del techo o mas.
                double rango = p.limiteMax() - p.limiteMin();
                double umbralAdvertencia = p.limiteMax() - rango * 0.15;
                if (lectura.valorMedido() >= umbralAdvertencia && rango > 0) {
                    Alerta advertencia = new Alerta(
                            contador.getAndIncrement(), lectura.idLectura(), 1,
                            p.nombreParametro(), lectura.valorMedido(), "Media",
                            "La concentracion de " + p.nombreParametro().toLowerCase()
                                    + " se acerca al limite permitido. Nivel actual: "
                                    + lectura.valorMedido() + " " + p.unidadMedida()
                                    + ". Limite: " + p.limiteMax() + " " + p.unidadMedida() + ".",
                            LocalDateTime.now());
                    alertas.add(advertencia);
                    return advertencia;
                }
                return null;
            }
            prioridad = p.nombreParametro().equalsIgnoreCase("Plomo") ? "Critica" : "Critica";
            descripcion = "La concentracion de " + p.nombreParametro().toLowerCase() + " ("
                    + lectura.valorMedido() + " " + p.unidadMedida() + ") supera el limite maximo permitido de "
                    + p.limiteMax() + " " + p.unidadMedida() + " segun la NOM-127-SSA1-2021.";
        }

        Alerta a = new Alerta(
                contador.getAndIncrement(), lectura.idLectura(), 1,
                p.nombreParametro(), lectura.valorMedido(), prioridad, descripcion,
                LocalDateTime.now());
        alertas.add(a);
        return a;
    }

    public List<Alerta> listar() {
        return alertas.stream()
                .sorted(Comparator.comparing(Alerta::fechaGeneracion).reversed())
                .toList();
    }
}
