package mx.edu.uttt.hydrosafe.configuracion;

public class ConfiguracionService {

    private Configuracion actual = new Configuracion(
            "HydroSafe", "v2.4.1",
            "Cisterna Principal", "CISTERNA-01",
            "UTTTLabIoT", -48,
            "ESP32", "Ing. Carlos Mendoza",
            "", ""
    );

    public Configuracion obtener() { return actual; }

    public Configuracion actualizar(Configuracion nueva) {
        this.actual = nueva;
        return actual;
    }
}
