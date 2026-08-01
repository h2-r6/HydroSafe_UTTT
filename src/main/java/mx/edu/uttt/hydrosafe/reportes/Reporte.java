package mx.edu.uttt.hydrosafe.reportes;

import java.time.LocalDateTime;

/** Corresponde a la entidad REPORTE del diccionario de datos (Actividad 25). */
public record Reporte(
        int idReporte,
        int idEncargado,
        String tipoReporte,
        LocalDateTime fechaGeneracion,
        String rutaArchivo
) {}
