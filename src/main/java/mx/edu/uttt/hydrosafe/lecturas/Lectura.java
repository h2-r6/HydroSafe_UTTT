package mx.edu.uttt.hydrosafe.lecturas;

import java.time.LocalDateTime;

/** Corresponde a la entidad LECTURA del diccionario de datos (Actividad 25). */
public record Lectura(
        int idLectura,
        int idParametro,
        double valorMedido,
        LocalDateTime marcaTemporal
) {}
