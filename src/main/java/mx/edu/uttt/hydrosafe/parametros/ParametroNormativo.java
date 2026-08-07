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
