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
package mx.edu.uttt.hydrosafe.eventos;

import com.sun.net.httpserver.HttpExchange;
import mx.edu.uttt.hydrosafe.web.Json;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Bus de eventos para Server-Sent Events (SSE): no es WebSocket (no es
 * bidireccional), pero para "avisame cuando algo cambie" es justo lo que se
 * necesita y es HTTP normal -- no requiere ninguna libreria.
 *
 * Cada suscriptor tiene su propia cola; el hilo que atiende esa conexion se
 * queda bloqueado leyendo de la cola (con timeout, para mandar un "ping" y no
 * morir por inactividad de proxies/navegador) hasta que el cliente se
 * desconecta -- ahi es cuando escribir al OutputStream lanza IOException y
 * limpiamos la suscripcion.
 */
public class EventBus {

    private final CopyOnWriteArrayList<BlockingQueue<String>> colas = new CopyOnWriteArrayList<>();

    public void atender(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/event-stream; charset=utf-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-cache");
        exchange.getResponseHeaders().set("Connection", "keep-alive");
        exchange.getResponseHeaders().set("X-Accel-Buffering", "no");
        exchange.sendResponseHeaders(200, 0); // 0 = chunked, sin longitud fija

        BlockingQueue<String> cola = new LinkedBlockingQueue<>();
        colas.add(cola);
        OutputStream os = exchange.getResponseBody();

        try {
            escribir(os, formatear("conectado", Map.of("mensaje", "Conectado a HydroSafe en tiempo real")));
            while (true) {
                String mensaje = cola.poll(25, TimeUnit.SECONDS);
                escribir(os, mensaje == null ? ": ping\n\n" : mensaje);
            }
        } catch (IOException clienteDesconectado) {
            // normal: el navegador cerro la pestana o navego a otra vista
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            colas.remove(cola);
            try { exchange.close(); } catch (Exception ignored) {}
        }
    }

    public void emitir(String tipo, Object payload) {
        String mensaje = formatear(tipo, payload);
        for (BlockingQueue<String> cola : colas) {
            cola.offer(mensaje);
        }
    }

    public int suscriptoresActivos() {
        return colas.size();
    }

    private String formatear(String tipo, Object payload) {
        return "event: " + tipo + "\ndata: " + Json.escribir(payload) + "\n\n";
    }

    private void escribir(OutputStream os, String texto) throws IOException {
        os.write(texto.getBytes(StandardCharsets.UTF_8));
        os.flush();
    }
}
