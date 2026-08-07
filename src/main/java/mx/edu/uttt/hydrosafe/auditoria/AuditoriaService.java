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
