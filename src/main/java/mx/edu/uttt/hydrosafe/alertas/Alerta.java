package mx.edu.uttt.hydrosafe.alertas;

import java.time.LocalDateTime;

public record Alerta(
        int idAlerta,
        int idLectura,
        int idEncargado,
        String parametro,
        double valorMedido,
        String prioridad,      // "Critica" | "Media"
        String descripcion,    // texto legible ya listo para mostrar en la UI
        LocalDateTime fechaGeneracion
) {}
