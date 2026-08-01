package mx.edu.uttt.hydrosafe.parametros;

/**
 * Corresponde a la entidad PARAMETRO_NORMATIVO del diccionario de datos (Actividad 25).
 * limiteMin/limiteMax generaliza el campo "Limite_Permisible" del PDF para cubrir
 * tanto techos simples (Plomo > 0.01) como rangos (pH entre 6.5 y 8.5).
 *
 * "tipo" distingue:
 *   - "concentracion": valor numerico continuo (Plomo, Cobre, Fluor, Sales/TDS)
 *   - "presencia":     0 = sin indicios, >0 = posible presencia (E. coli)
 *   - "rango":         hay que estar entre min y max (para futuros parametros como pH)
 */
public record ParametroNormativo(
        int idParametro,
        String nombreParametro,
        String unidadMedida,
        double limiteMin,
        double limiteMax,
        String tipo
) {}
