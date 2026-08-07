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
package mx.edu.uttt.hydrosafe.resumen;

import com.sun.net.httpserver.HttpExchange;
import mx.edu.uttt.hydrosafe.configuracion.Configuracion;
import mx.edu.uttt.hydrosafe.configuracion.ConfiguracionService;
import mx.edu.uttt.hydrosafe.lecturas.Lectura;
import mx.edu.uttt.hydrosafe.lecturas.LecturaService;
import mx.edu.uttt.hydrosafe.parametros.ParametroNormativo;
import mx.edu.uttt.hydrosafe.parametros.ParametroService;
import mx.edu.uttt.hydrosafe.web.JsonHttp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Resumen de solo lectura, PUBLICO (sin login), para la pantalla de inicio de
 * sesion -- un vistazo del estado del agua antes de entrar. No expone nada
 * sensible: nomas los valores actuales y su estado, igual que veria cualquiera
 * parado frente al tablero fisico del laboratorio.
 */
public class ResumenPublicoController {

    private final ParametroService parametroService;
    private final LecturaService lecturaService;
    private final ConfiguracionService configuracionService;

    public ResumenPublicoController(ParametroService p, LecturaService l, ConfiguracionService c) {
        this.parametroService = p;
        this.lecturaService = l;
        this.configuracionService = c;
    }

    public void obtener(HttpExchange e) throws IOException {
        List<ParametroNormativo> parametros = parametroService.listar();
        List<Lectura> ultimas = lecturaService.ultimasPorParametro();

        List<Map<String, Object>> filas = new ArrayList<>();
        String peor = "verde";

        for (ParametroNormativo p : parametros) {
            Lectura l = ultimas.stream().filter(x -> x.idParametro() == p.idParametro()).findFirst().orElse(null);
            Double valor = l != null ? l.valorMedido() : null;
            Map<String, String> estado = calcularEstado(p, valor);
            if ("rojo".equals(estado.get("clase"))) {
                peor = "rojo";
            } else if ("amber".equals(estado.get("clase")) && !"rojo".equals(peor)) {
                peor = "amber";
            }

            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("nombreParametro", p.nombreParametro());
            fila.put("unidadMedida", p.unidadMedida());
            fila.put("tipo", p.tipo());
            fila.put("valorMedido", valor);
            fila.put("estadoClase", estado.get("clase"));
            fila.put("estadoTexto", estado.get("texto"));
            filas.add(fila);
        }

        Map<String, String> textos = Map.of("verde", "Buena", "amber", "Precaución", "rojo", "Riesgo");

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("estadoGeneral", Map.of("clase", peor, "texto", textos.get(peor)));
        respuesta.put("parametros", filas);
        Configuracion config = configuracionService.obtener();
        respuesta.put("nodo", Map.of("nodeId", config.nodeId(), "ubicacionNodo", config.ubicacionNodo()));

        JsonHttp.enviar(e, 200, respuesta);
    }

    private Map<String, String> calcularEstado(ParametroNormativo p, Double valor) {
        if (valor == null) return Map.of("clase", "verde", "texto", "Sin datos");
        if ("presencia".equals(p.tipo())) {
            return valor > 0 ? Map.of("clase", "rojo", "texto", "Riesgo") : Map.of("clase", "verde", "texto", "Buena");
        }
        if (valor < p.limiteMin() || valor > p.limiteMax()) return Map.of("clase", "rojo", "texto", "Riesgo");
        double rango = p.limiteMax() - p.limiteMin();
        if (rango > 0 && valor >= p.limiteMax() - rango * 0.15) return Map.of("clase", "amber", "texto", "Precaución");
        return Map.of("clase", "verde", "texto", "Buena");
    }
}
