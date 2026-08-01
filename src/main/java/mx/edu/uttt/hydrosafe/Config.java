package mx.edu.uttt.hydrosafe;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Lee config/server.properties desde la raíz del proyecto (no desde el classpath),
 * igual que en el proyecto original. Si no lo encuentra, usa valores por defecto.
 */
public class Config {

    private static final Properties props = new Properties();

    static {
        try (FileInputStream in = new FileInputStream("config/server.properties")) {
            props.load(in);
        } catch (IOException e) {
            System.out.println("[Config] No se encontro config/server.properties, usando valores por defecto.");
        }
    }

    public static int getPort() {
        return Integer.parseInt(props.getProperty("server.port", "7000"));
    }

    public static String getAppName() {
        return props.getProperty("app.name", "HydroSafe");
    }
}
