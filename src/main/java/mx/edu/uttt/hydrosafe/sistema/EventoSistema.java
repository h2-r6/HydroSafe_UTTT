package mx.edu.uttt.hydrosafe.sistema;

import java.time.LocalDateTime;

/** tipo: "arranque" | "error" | "config" | "info" */
public record EventoSistema(int id, String tipo, String mensaje, LocalDateTime fecha) {}
