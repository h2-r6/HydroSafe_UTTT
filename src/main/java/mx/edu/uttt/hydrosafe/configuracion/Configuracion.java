package mx.edu.uttt.hydrosafe.configuracion;

/**
 * Configuracion editable del sistema. videoUrl/githubUrl/nombreProyecto son
 * para la pagina de Documentacion (/docs), donde se puede pegar el link del
 * video demo y del repo cuando esten listos.
 */
public record Configuracion(
        String nombreApp,
        String version,
        String ubicacionNodo,
        String nodeId,
        String wifiSSID,
        int wifiRSSI,
        String microcontrolador,
        String encargadoResponsable,
        String videoUrl,
        String githubUrl
) {}
