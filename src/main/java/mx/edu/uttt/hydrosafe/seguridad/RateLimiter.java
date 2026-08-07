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
package mx.edu.uttt.hydrosafe.seguridad;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Bloqueo temporal por correo despues de varios intentos fallidos de login.
 * En memoria (si reinicias el server se resetea). No bloquea por IP porque en
 * una red universitaria muchos equipos comparten la misma IP (NAT del lab).
 */
public class RateLimiter {

    private static final int MAX_INTENTOS = 5;
    private static final long BLOQUEO_SEGUNDOS = 60;

    private record Estado(AtomicInteger intentos, AtomicLong bloqueadoHasta) {}

    private final ConcurrentHashMap<String, Estado> estados = new ConcurrentHashMap<>();

    public long segundosDeBloqueo(String correo) {
        Estado e = estados.get(clave(correo));
        if (e == null) return 0;
        long restante = e.bloqueadoHasta().get() - Instant.now().getEpochSecond();
        return Math.max(0, restante);
    }

    public void registrarFallo(String correo) {
        Estado e = estados.computeIfAbsent(clave(correo), k -> new Estado(new AtomicInteger(0), new AtomicLong(0)));
        int intentos = e.intentos().incrementAndGet();
        if (intentos >= MAX_INTENTOS) {
            e.bloqueadoHasta().set(Instant.now().getEpochSecond() + BLOQUEO_SEGUNDOS);
            e.intentos().set(0);
        }
    }

    public void registrarExito(String correo) {
        estados.remove(clave(correo));
    }

    private String clave(String correo) {
        return correo == null ? "" : correo.toLowerCase();
    }
}
