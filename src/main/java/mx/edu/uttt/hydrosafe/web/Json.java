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

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON hecho a mano: sabe leer/escribir Map, List, records, numeros, booleanos,
 * String y LocalDateTime. No es Jackson (no maneja todos los casos raros), pero
 * para esto alcanza y sobra, y con esto el proyecto entero compila y corre con
 * SOLO el JDK — cero dependencias, cero Gradle bajando nada de Maven Central.
 *
 * Si mas adelante quieres algo mas robusto, es un cambio chico: agregas
 * jackson-databind en build.gradle.kts y cambias las 2 llamadas que usa
 * JsonHttp (escribir/leer) por las de ObjectMapper.
 */
public class Json {

    private Json() {}

    // ---------- escribir ----------

    public static String escribir(Object o) {
        StringBuilder sb = new StringBuilder();
        escribirValor(o, sb);
        return sb.toString();
    }

    private static void escribirValor(Object o, StringBuilder sb) {
        if (o == null) {
            sb.append("null");
        } else if (o instanceof String s) {
            escribirString(s, sb);
        } else if (o instanceof LocalDateTime dt) {
            escribirString(dt.toString(), sb);
        } else if (o instanceof Number || o instanceof Boolean) {
            sb.append(o);
        } else if (o instanceof Map<?, ?> map) {
            sb.append('{');
            boolean primero = true;
            for (Map.Entry<?, ?> e : map.entrySet()) {
                if (!primero) sb.append(',');
                primero = false;
                escribirString(String.valueOf(e.getKey()), sb);
                sb.append(':');
                escribirValor(e.getValue(), sb);
            }
            sb.append('}');
        } else if (o instanceof Iterable<?> it) {
            sb.append('[');
            boolean primero = true;
            for (Object item : it) {
                if (!primero) sb.append(',');
                primero = false;
                escribirValor(item, sb);
            }
            sb.append(']');
        } else if (o.getClass().isRecord()) {
            sb.append('{');
            RecordComponent[] comps = o.getClass().getRecordComponents();
            boolean primero = true;
            try {
                for (RecordComponent comp : comps) {
                    if (!primero) sb.append(',');
                    primero = false;
                    escribirString(comp.getName(), sb);
                    sb.append(':');
                    escribirValor(comp.getAccessor().invoke(o), sb);
                }
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
            sb.append('}');
        } else {
            escribirString(o.toString(), sb);
        }
    }

    private static void escribirString(String s, StringBuilder sb) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        sb.append('"');
    }

    // ---------- leer ----------

    public static <T> T leer(String texto, Class<T> tipo) {
        Object parseado = parsear(texto);
        return convertirA(parseado, tipo);
    }

    @SuppressWarnings("unchecked")
    private static <T> T convertirA(Object valor, Class<T> tipo) {
        if (valor == null) return null;
        if (tipo.isRecord()) {
            if (!(valor instanceof Map)) {
                throw new RuntimeException("Se esperaba un objeto JSON para " + tipo.getSimpleName());
            }
            Map<String, Object> map = (Map<String, Object>) valor;
            RecordComponent[] comps = tipo.getRecordComponents();
            Class<?>[] tiposParam = new Class<?>[comps.length];
            Object[] args = new Object[comps.length];
            for (int i = 0; i < comps.length; i++) {
                tiposParam[i] = comps[i].getType();
                args[i] = convertirCampo(map.get(comps[i].getName()), comps[i]);
            }
            try {
                Constructor<T> ctor = tipo.getDeclaredConstructor(tiposParam);
                return ctor.newInstance(args);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
        return (T) valor;
    }

    @SuppressWarnings("unchecked")
    private static Object convertirCampo(Object valor, RecordComponent comp) {
        Class<?> tipo = comp.getType();
        if (valor == null) {
            // Los primitivos no aceptan null: caemos a su cero por defecto para que el
            // record se pueda construir cuando el cliente omite un campo opcional.
            if (tipo == int.class) return 0;
            if (tipo == long.class) return 0L;
            if (tipo == double.class) return 0.0;
            if (tipo == float.class) return 0.0f;
            if (tipo == boolean.class) return false;
            return null;
        }
        if (tipo == String.class) return String.valueOf(valor);
        if (tipo == Double.class || tipo == double.class) return ((Number) valor).doubleValue();
        if (tipo == Integer.class || tipo == int.class) return ((Number) valor).intValue();
        if (tipo == Boolean.class || tipo == boolean.class) return valor;
        if (tipo == LocalDateTime.class) return LocalDateTime.parse(String.valueOf(valor));
        // List<Record>: leemos el tipo generico del RecordComponent y convertimos elemento por elemento.
        if (tipo == List.class && valor instanceof List<?> lista) {
            java.lang.reflect.Type generico = comp.getGenericType();
            if (generico instanceof java.lang.reflect.ParameterizedType pt) {
                java.lang.reflect.Type argTipo = pt.getActualTypeArguments()[0];
                if (argTipo instanceof Class<?> clase && clase.isRecord()) {
                    List<Object> convertida = new ArrayList<>();
                    for (Object item : lista) convertida.add(convertirA(item, clase));
                    return convertida;
                }
            }
            return lista;
        }
        return valor;
    }

    // ---------- parser generico (Map/List/String/Double/Boolean/null) ----------

    private static Object parsear(String s) {
        int[] pos = {0};
        return parsearValor(s, pos);
    }

    private static void saltarEspacios(String s, int[] pos) {
        while (pos[0] < s.length() && Character.isWhitespace(s.charAt(pos[0]))) pos[0]++;
    }

    private static Object parsearValor(String s, int[] pos) {
        saltarEspacios(s, pos);
        char c = s.charAt(pos[0]);
        if (c == '{') return parsearObjeto(s, pos);
        if (c == '[') return parsearArreglo(s, pos);
        if (c == '"') return parsearString(s, pos);
        if (s.startsWith("null", pos[0])) { pos[0] += 4; return null; }
        if (s.startsWith("true", pos[0])) { pos[0] += 4; return Boolean.TRUE; }
        if (s.startsWith("false", pos[0])) { pos[0] += 5; return Boolean.FALSE; }
        return parsearNumero(s, pos);
    }

    private static Map<String, Object> parsearObjeto(String s, int[] pos) {
        Map<String, Object> map = new LinkedHashMap<>();
        pos[0]++; // {
        saltarEspacios(s, pos);
        if (s.charAt(pos[0]) == '}') { pos[0]++; return map; }
        while (true) {
            saltarEspacios(s, pos);
            String clave = parsearString(s, pos);
            saltarEspacios(s, pos);
            pos[0]++; // :
            Object valor = parsearValor(s, pos);
            map.put(clave, valor);
            saltarEspacios(s, pos);
            char c = s.charAt(pos[0]);
            if (c == ',') { pos[0]++; continue; }
            if (c == '}') { pos[0]++; break; }
        }
        return map;
    }

    private static List<Object> parsearArreglo(String s, int[] pos) {
        List<Object> lista = new ArrayList<>();
        pos[0]++; // [
        saltarEspacios(s, pos);
        if (s.charAt(pos[0]) == ']') { pos[0]++; return lista; }
        while (true) {
            lista.add(parsearValor(s, pos));
            saltarEspacios(s, pos);
            char c = s.charAt(pos[0]);
            if (c == ',') { pos[0]++; continue; }
            if (c == ']') { pos[0]++; break; }
        }
        return lista;
    }

    private static String parsearString(String s, int[] pos) {
        pos[0]++; // "
        StringBuilder sb = new StringBuilder();
        while (s.charAt(pos[0]) != '"') {
            char c = s.charAt(pos[0]);
            if (c == '\\') {
                pos[0]++;
                char esc = s.charAt(pos[0]);
                switch (esc) {
                    case 'n' -> sb.append('\n');
                    case 't' -> sb.append('\t');
                    case 'r' -> sb.append('\r');
                    case '"' -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    case '/' -> sb.append('/');
                    case 'u' -> {
                        String hex = s.substring(pos[0] + 1, pos[0] + 5);
                        sb.append((char) Integer.parseInt(hex, 16));
                        pos[0] += 4;
                    }
                    default -> sb.append(esc);
                }
            } else {
                sb.append(c);
            }
            pos[0]++;
        }
        pos[0]++; // "
        return sb.toString();
    }

    private static Double parsearNumero(String s, int[] pos) {
        int inicio = pos[0];
        while (pos[0] < s.length() && "-+.eE0123456789".indexOf(s.charAt(pos[0])) >= 0) pos[0]++;
        return Double.parseDouble(s.substring(inicio, pos[0]));
    }
}
