package mx.edu.uttt.hydrosafe;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Utilidades compartidas. Punto de partida minimo, ve agregando lo que necesites aqui. */
public class Util {

    private static final DateTimeFormatter FORMATO_LEGIBLE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public static String ahora() {
        return LocalDateTime.now().format(FORMATO_LEGIBLE);
    }
}
