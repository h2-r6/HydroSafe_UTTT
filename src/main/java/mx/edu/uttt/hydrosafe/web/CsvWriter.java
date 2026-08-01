package mx.edu.uttt.hydrosafe.web;

import mx.edu.uttt.hydrosafe.lecturas.Lectura;
import mx.edu.uttt.hydrosafe.parametros.ParametroNormativo;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CsvWriter {

    public static byte[] deLecturas(List<Lectura> lecturas, List<ParametroNormativo> parametros) {
        Map<Integer, String> nombres = new HashMap<>();
        for (ParametroNormativo p : parametros) nombres.put(p.idParametro(), p.nombreParametro() + " (" + p.unidadMedida() + ")");

        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF'); // BOM para que Excel detecte UTF-8 y no rompa acentos
        sb.append("Fecha,Parametro,Valor\n");
        for (Lectura l : lecturas) {
            sb.append(escapar(l.marcaTemporal().toString())).append(',')
              .append(escapar(nombres.getOrDefault(l.idParametro(), "Parametro " + l.idParametro()))).append(',')
              .append(l.valorMedido()).append('\n');
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static String escapar(String s) {
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}
