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
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Generador minimo de archivos .xlsx: una sola hoja, celdas de texto (inline,
 * sin sharedStrings) o numero, sin estilos. No usa Apache POI porque no hay
 * acceso a Maven Central aqui -- pero un .xlsx al final es solo un ZIP con
 * unos XML adentro, y java.util.zip.ZipOutputStream ya viene en el JDK.
 */
public class MiniXlsx {

    public static byte[] generar(String nombreHoja, List<String> encabezados, List<List<Object>> filas) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(buffer)) {
            escribir(zip, "[Content_Types].xml", contentTypes());
            escribir(zip, "_rels/.rels", relsRaiz());
            escribir(zip, "xl/workbook.xml", workbook(nombreHoja));
            escribir(zip, "xl/_rels/workbook.xml.rels", workbookRels());
            escribir(zip, "xl/worksheets/sheet1.xml", hoja(encabezados, filas));
        }
        return buffer.toByteArray();
    }

    private static void escribir(ZipOutputStream zip, String nombre, String contenido) throws IOException {
        zip.putNextEntry(new ZipEntry(nombre));
        zip.write(contenido.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private static String contentTypes() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
             + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
             + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
             + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
             + "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>"
             + "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>"
             + "</Types>";
    }

    private static String relsRaiz() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
             + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
             + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>"
             + "</Relationships>";
    }

    private static String workbook(String nombreHoja) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
             + "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" "
             + "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
             + "<sheets><sheet name=\"" + escaparXml(nombreHoja) + "\" sheetId=\"1\" r:id=\"rId1\"/></sheets>"
             + "</workbook>";
    }

    private static String workbookRels() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
             + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
             + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>"
             + "</Relationships>";
    }

    private static String hoja(List<String> encabezados, List<List<Object>> filas) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        sb.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><sheetData>");
        sb.append(fila(1, encabezados.stream().map(h -> (Object) h).toList()));
        int numFila = 2;
        for (List<Object> fila : filas) {
            sb.append(fila(numFila, fila));
            numFila++;
        }
        sb.append("</sheetData></worksheet>");
        return sb.toString();
    }

    private static String fila(int numFila, List<Object> valores) {
        StringBuilder sb = new StringBuilder("<row r=\"" + numFila + "\">");
        for (int col = 0; col < valores.size(); col++) {
            String celdaRef = columna(col) + numFila;
            Object v = valores.get(col);
            if (v instanceof Number) {
                sb.append("<c r=\"").append(celdaRef).append("\"><v>").append(v).append("</v></c>");
            } else {
                sb.append("<c r=\"").append(celdaRef).append("\" t=\"inlineStr\"><is><t xml:space=\"preserve\">")
                  .append(escaparXml(String.valueOf(v))).append("</t></is></c>");
            }
        }
        sb.append("</row>");
        return sb.toString();
    }

    private static String columna(int indiceCero) {
        StringBuilder letras = new StringBuilder();
        int n = indiceCero;
        do {
            letras.insert(0, (char) ('A' + (n % 26)));
            n = n / 26 - 1;
        } while (n >= 0);
        return letras.toString();
    }

    private static String escaparXml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }
}
