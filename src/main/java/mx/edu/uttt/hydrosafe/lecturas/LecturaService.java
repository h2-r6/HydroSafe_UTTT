/**
 * HydroSafe (UTTT) — Sistema de Monitoreo de la Calidad del Agua
 * 
 * Autores:
 *   - Maria Fernanda Aldana Jiménez
 *   - Natali Isabel Chávez Alpízar
 *   - Hiyadir Raúlciel Barrera Cuervo
 * 
 * Universidad Tecnológica de Tula-Tepeji
 * Programa Educativo: Ingeniería en Tecnologías de la Información, área Infraestructura de Redes Inteligentes y Ciberseguridad
 * Empresa: Universidad Tecnológica de Tula-Tepeji
 * 
 * Asesor Académico: M. en C. Odisey Yasmin Porras Beltrán
 * Asesores Colaboradores: Marisol Reséndiz Vega, Mario Herrera Telles
 * 
 * Este software fue desarrollado durante el cuatrimestre mayo-agosto 2026.
 * Los derechos morales pertenecen a sus autores.
 * Queda prohibida la eliminación de los créditos originales y el uso o modificación del código sin autorización de los autores.
 */
package mx.edu.uttt.hydrosafe.lecturas;

import mx.edu.uttt.hydrosafe.alertas.Alerta;
import mx.edu.uttt.hydrosafe.alertas.AlertaService;
import mx.edu.uttt.hydrosafe.eventos.EventBus;
import mx.edu.uttt.hydrosafe.parametros.ParametroNormativo;
import mx.edu.uttt.hydrosafe.parametros.ParametroService;
import mx.edu.uttt.hydrosafe.semaforo.SemaforoService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class LecturaService {

    private final List<Lectura> lecturas = new ArrayList<>();
    private final AtomicInteger contador = new AtomicInteger(1);
    private final ParametroService parametroService;
    private final AlertaService alertaService;
    private final SemaforoService semaforoService;
    private final EventBus eventBus;

    public LecturaService(ParametroService parametroService, AlertaService alertaService,
                           SemaforoService semaforoService, EventBus eventBus) {
        this.parametroService = parametroService;
        this.alertaService = alertaService;
        this.semaforoService = semaforoService;
        this.eventBus = eventBus;
        sembrarDatosDemo();
    }

    public List<Lectura> registrar(NuevaLecturaRequest req) {
        LocalDateTime marca = req.marcaTemporal() != null ? req.marcaTemporal() : LocalDateTime.now();
        List<Lectura> nuevas = new ArrayList<>();
        if (req.mediciones() == null) return nuevas;

        for (NuevaLecturaRequest.Medicion m : req.mediciones()) {
            Lectura l = new Lectura(contador.getAndIncrement(), m.idParametro(), m.valorMedido(), marca);
            lecturas.add(l);
            nuevas.add(l);

            eventBus.emitir("lectura", l);
            Alerta a = alertaService.evaluar(l);
            if (a != null) eventBus.emitir("alerta", a);
        }

        semaforoService.registrar(calcularEstadoGeneral());
        return nuevas;
    }

    public List<Lectura> ultimasPorParametro() {
        return parametroService.listar().stream()
                .map(p -> lecturas.stream()
                        .filter(l -> l.idParametro() == p.idParametro())
                        .max(Comparator.comparing(Lectura::marcaTemporal))
                        .orElse(null))
                .filter(Objects::nonNull)
                .toList();
    }

    /** Lecturas de los ultimos N minutos para un parametro (usado en la grafica en vivo del dashboard). */
    public List<Lectura> ultimosMinutos(int idParametro, int minutos) {
        LocalDateTime desde = LocalDateTime.now().minusMinutes(minutos);
        return lecturas.stream()
                .filter(l -> l.idParametro() == idParametro)
                .filter(l -> !l.marcaTemporal().isBefore(desde))
                .sorted(Comparator.comparing(Lectura::marcaTemporal))
                .toList();
    }

    public List<Lectura> historial(Integer idParametro, LocalDateTime desde, LocalDateTime hasta) {
        return lecturas.stream()
                .filter(l -> idParametro == null || l.idParametro() == idParametro)
                .filter(l -> desde == null || !l.marcaTemporal().isBefore(desde))
                .filter(l -> hasta == null || !l.marcaTemporal().isAfter(hasta))
                .sorted(Comparator.comparing(Lectura::marcaTemporal).reversed())
                .toList();
    }

    /** Misma logica de "que tan cerca del limite estamos" que usa el frontend, para poder registrarla en el historial del semaforo. */
    private String calcularEstadoGeneral() {
        String peor = "verde";
        for (Lectura l : ultimasPorParametro()) {
            ParametroNormativo p = parametroService.porId(l.idParametro());
            if (p == null) continue;
            if ("presencia".equals(p.tipo())) {
                if (l.valorMedido() > 0) return "rojo";
                continue;
            }
            if (l.valorMedido() < p.limiteMin() || l.valorMedido() > p.limiteMax()) return "rojo";
            double rango = p.limiteMax() - p.limiteMin();
            if (rango > 0 && l.valorMedido() >= p.limiteMax() - rango * 0.15) peor = "amber";
        }
        return peor;
    }

    private void sembrarDatosDemo() {
        LocalDateTime base = LocalDateTime.now().minusHours(24);
        // Parametros: 1=Fluor, 2=Cobre, 3=Plomo, 4=Sales, 5=EColi
        double[] fluor = {0.62, 0.65, 0.68, 0.70, 0.69, 0.71, 0.75, 0.72};
        double[] cobre = {0.90, 1.00, 1.10, 1.20, 1.15, 1.24, 1.30, 1.28};
        double[] plomo = {0.005, 0.006, 0.007, 0.008, 0.009, 0.010, 0.011, 0.012};
        double[] sales = {320, 340, 360, 370, 365, 375, 380, 385};
        double[] eColi = {0, 0, 0, 0, 0, 0, 0, 1};
        for (int i = 0; i < 8; i++) {
            LocalDateTime t = base.plusHours(i * 3L);
            registrarPuntoSemilla(1, fluor[i], t);
            registrarPuntoSemilla(2, cobre[i], t);
            registrarPuntoSemilla(3, plomo[i], t);
            registrarPuntoSemilla(4, sales[i], t);
            registrarPuntoSemilla(5, eColi[i], t);
        }
        // Ultimos 60 minutos con pasos de 5 min, para que la grafica "en vivo" del dashboard tenga contenido real desde el arranque.
        LocalDateTime baseReciente = LocalDateTime.now().minusMinutes(55);
        for (int i = 0; i < 12; i++) {
            LocalDateTime t = baseReciente.plusMinutes(i * 5L);
            registrarPuntoSemilla(1, 0.68 + Math.sin(i * 0.5) * 0.05, t);
            registrarPuntoSemilla(2, 1.20 + Math.cos(i * 0.4) * 0.08, t);
            registrarPuntoSemilla(3, 0.009 + (i * 0.0003), t);
            registrarPuntoSemilla(4, 370 + Math.sin(i * 0.3) * 12, t);
            registrarPuntoSemilla(5, 0, t);
        }
    }

    private void registrarPuntoSemilla(int idParametro, double valor, LocalDateTime t) {
        Lectura l = new Lectura(contador.getAndIncrement(), idParametro, valor, t);
        lecturas.add(l);
        alertaService.evaluar(l);
    }
}
