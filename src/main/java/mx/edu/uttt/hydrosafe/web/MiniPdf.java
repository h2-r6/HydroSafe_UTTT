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
package mx.edu.uttt.hydrosafe.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Generador minimo de PDF (texto + lineas simples, tamano Carta 612x792pt,
 * paginacion automatica). No usa iText/PDFBox porque no hay acceso a Maven
 * Central aqui -- un PDF basico es en el fondo texto plano con una tabla de
 * referencias (xref) al final indicando el byte exacto de cada objeto, asi
 * que se puede armar a mano con cuidado.
 *
 * Soporta lo suficiente para un reporte: titulo, subtitulo, parrafos, lineas
 * horizontales y una tabla simple con columnas de igual ancho.
 */
public class MiniPdf {

    private static final double ANCHO = 612;
    private static final double ALTO = 792;
    private static final double MARGEN = 50;

    private final List<String> streamsPorPagina = new ArrayList<>();
    private StringBuilder streamActual;
    private double cursorY;

    public MiniPdf() {
        streamActual = new StringBuilder();
        cursorY = ALTO - MARGEN;
    }

    private void nuevaPagina() {
        streamsPorPagina.add(streamActual.toString());
        streamActual = new StringBuilder();
        cursorY = ALTO - MARGEN;
    }

    private void asegurarEspacio(double alturaNecesaria) {
        if (cursorY - alturaNecesaria < MARGEN) nuevaPagina();
    }

    public MiniPdf titulo(String texto) {
        asegurarEspacio(30);
        cursorY -= 22;
        escribirTexto(texto, MARGEN, cursorY, "F2", 18);
        cursorY -= 4;
        return this;
    }

    public MiniPdf subtitulo(String texto) {
        asegurarEspacio(20);
        cursorY -= 16;
        escribirTexto(texto, MARGEN, cursorY, "F1", 11);
        return this;
    }

    public MiniPdf parrafo(String texto) {
        asegurarEspacio(16);
        cursorY -= 14;
        escribirTexto(texto, MARGEN, cursorY, "F1", 10);
        return this;
    }

    public MiniPdf espacio(double puntos) {
        cursorY -= puntos;
        return this;
    }

    public MiniPdf linea() {
        asegurarEspacio(8);
        cursorY -= 4;
        streamActual.append(String.format(Locale.US, "%.2f %.2f m %.2f %.2f l S%n", MARGEN, cursorY, ANCHO - MARGEN, cursorY));
        cursorY -= 6;
        return this;
    }

    /** Tabla con columnas de igual ancho (suficiente para un reporte tabular simple). */
    public MiniPdf tabla(List<String> encabezados, List<List<String>> filas) {
        double anchoTotal = ANCHO - 2 * MARGEN;
        double anchoColumna = anchoTotal / encabezados.size();

        asegurarEspacio(20);
        cursorY -= 14;
        double x = MARGEN;
        for (String enc : encabezados) {
            escribirTexto(enc, x, cursorY, "F2", 8.5);
            x += anchoColumna;
        }
        cursorY -= 4;
        linea();

        for (List<String> fila : filas) {
            asegurarEspacio(16);
            cursorY -= 12;
            double xf = MARGEN;
            for (int i = 0; i < fila.size() && i < encabezados.size(); i++) {
                escribirTexto(recortar(fila.get(i), anchoColumna), xf, cursorY, "F1", 8.5);
                xf += anchoColumna;
            }
        }
        return this;
    }

    private String recortar(String s, double anchoDisponible) {
        int maxCaracteres = (int) (anchoDisponible / 4.6); // aprox. ancho de caracter en Helvetica 8.5pt
        if (s.length() > maxCaracteres && maxCaracteres > 3) return s.substring(0, maxCaracteres - 1) + ".";
        return s;
    }

    private void escribirTexto(String texto, double x, double y, String fuente, double tamano) {
        streamActual.append(String.format(Locale.US, "BT /%s %.1f Tf %.2f %.2f Td (%s) Tj ET%n",
                fuente, tamano, x, y, escaparPdf(texto)));
    }

    private String escaparPdf(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '(' || c == ')' || c == '\\') sb.append('\\').append(c);
            else if (c <= 0xFF) sb.append(c); // WinAnsiEncoding cubre acentos/enye latinos directo
            else sb.append('?');
        }
        return sb.toString();
    }

    public byte[] generar() throws IOException {
        streamsPorPagina.add(streamActual.toString());
        int numPaginas = streamsPorPagina.size();

        final int OBJ_CATALOGO = 1, OBJ_PAGINAS = 2, OBJ_FUENTE_REGULAR = 3, OBJ_FUENTE_NEGRITA = 4;
        final int primeraPagina = 5;
        final int primerContenido = primeraPagina + numPaginas;
        final int totalObjetosReales = 4 + 2 * numPaginas; // ids 1..totalObjetosReales

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<Integer> offsets = new ArrayList<>();

        out.write("%PDF-1.4\n%\u00E2\u00E3\u00CF\u00D3\n".getBytes(StandardCharsets.ISO_8859_1));

        offsets.add(out.size());
        escribirObj(out, OBJ_CATALOGO + " 0 obj\n<< /Type /Catalog /Pages " + OBJ_PAGINAS + " 0 R >>\nendobj\n");

        offsets.add(out.size());
        StringBuilder kids = new StringBuilder();
        for (int i = 0; i < numPaginas; i++) kids.append(primeraPagina + i).append(" 0 R ");
        escribirObj(out, OBJ_PAGINAS + " 0 obj\n<< /Type /Pages /Kids [" + kids.toString().trim() + "] /Count " + numPaginas + " >>\nendobj\n");

        offsets.add(out.size());
        escribirObj(out, OBJ_FUENTE_REGULAR + " 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>\nendobj\n");

        offsets.add(out.size());
        escribirObj(out, OBJ_FUENTE_NEGRITA + " 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>\nendobj\n");

        for (int i = 0; i < numPaginas; i++) {
            offsets.add(out.size());
            int idPagina = primeraPagina + i;
            int idContenido = primerContenido + i;
            escribirObj(out, idPagina + " 0 obj\n<< /Type /Page /Parent " + OBJ_PAGINAS + " 0 R /MediaBox [0 0 " + (int) ANCHO + " " + (int) ALTO + "] "
                    + "/Resources << /Font << /F1 " + OBJ_FUENTE_REGULAR + " 0 R /F2 " + OBJ_FUENTE_NEGRITA + " 0 R >> >> "
                    + "/Contents " + idContenido + " 0 R >>\nendobj\n");
        }

        for (int i = 0; i < numPaginas; i++) {
            offsets.add(out.size());
            int idContenido = primerContenido + i;
            byte[] streamBytes = streamsPorPagina.get(i).getBytes(StandardCharsets.ISO_8859_1);
            escribirObj(out, idContenido + " 0 obj\n<< /Length " + streamBytes.length + " >>\nstream\n");
            out.write(streamBytes);
            escribirObj(out, "\nendstream\nendobj\n");
        }

        int xrefOffset = out.size();
        int countXref = totalObjetosReales + 1; // +1 por el objeto 0 (cabeza de la lista libre)
        escribirObj(out, "xref\n0 " + countXref + "\n");
        escribirObj(out, "0000000000 65535 f \n");
        for (int off : offsets) {
            escribirObj(out, String.format(Locale.US, "%010d 00000 n \n", off));
        }
        escribirObj(out, "trailer\n<< /Size " + countXref + " /Root " + OBJ_CATALOGO + " 0 R >>\nstartxref\n" + xrefOffset + "\n%%EOF");

        return out.toByteArray();
    }

    private void escribirObj(ByteArrayOutputStream out, String texto) throws IOException {
        out.write(texto.getBytes(StandardCharsets.ISO_8859_1));
    }
}
