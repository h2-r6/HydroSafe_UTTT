package mx.edu.uttt.hydrosafe.web;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/** Utilidades minimas para hablar JSON con com.sun.net.httpserver, sin depender de ningun framework ni libreria externa. */
public class JsonHttp {

    public static void enviar(HttpExchange exchange, int status, Object cuerpo) throws IOException {
        byte[] bytes = Json.escribir(cuerpo).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    public static void enviarVacio(HttpExchange exchange, int status) throws IOException {
        exchange.sendResponseHeaders(status, -1);
        exchange.close();
    }

    public static <T> T leerCuerpo(HttpExchange exchange, Class<T> tipo) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            String texto = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return Json.leer(texto, tipo);
        }
    }

    public static Map<String, String> queryParams(HttpExchange exchange) {
        Map<String, String> params = new LinkedHashMap<>();
        String query = exchange.getRequestURI().getRawQuery();
        if (query == null || query.isBlank()) return params;
        for (String par : query.split("&")) {
            String[] kv = par.split("=", 2);
            String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
            String value = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
            params.put(key, value);
        }
        return params;
    }
}
