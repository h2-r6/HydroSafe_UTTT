package mx.edu.uttt.hydrosafe.auditoria;

import java.time.LocalDateTime;

/** Quien hizo que, en el panel de administracion. */
public record EventoAuditoria(int id, String usuario, String accion, String detalle, LocalDateTime fecha) {}
