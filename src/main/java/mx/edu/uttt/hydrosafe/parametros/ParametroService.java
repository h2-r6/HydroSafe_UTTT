package mx.edu.uttt.hydrosafe.parametros;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Catalogo mutable de parametros normativos. En la vista Encargado de Monitoreo
 * los limites son de solo lectura (regla "Inmutabilidad de umbrales" del PDF),
 * pero desde el panel Admin se pueden ajustar (para preparar la app para futuros
 * cambios normativos o para agregar sensores nuevos sin recompilar).
 */
public class ParametroService {

    private final List<ParametroNormativo> catalogo = new ArrayList<>();
    private final AtomicInteger contador = new AtomicInteger(1);

    public ParametroService() {
        // Los 5 parametros de los mockups. Los primeros 3 son los del Acta de
        // Alcance del PDF; Sales/TDS y E. coli se agregaron para extender el
        // sistema (el PDF los excluia originalmente, pero cliente los pidio).
        crear("Flúor", "mg/L", 0.0, 1.5, "concentracion");
        crear("Cobre", "mg/L", 0.0, 2.0, "concentracion");
        crear("Plomo", "mg/L", 0.0, 0.01, "concentracion");
        crear("Sales (TDS)", "mg/L", 0.0, 500.0, "concentracion");
        crear("Posible E. coli", "índice", 0.0, 0.0, "presencia");
    }

    public List<ParametroNormativo> listar() { return List.copyOf(catalogo); }

    public ParametroNormativo porId(int id) {
        return catalogo.stream().filter(p -> p.idParametro() == id).findFirst().orElse(null);
    }

    public ParametroNormativo crear(String nombre, String unidad, double min, double max, String tipo) {
        ParametroNormativo p = new ParametroNormativo(contador.getAndIncrement(), nombre, unidad, min, max, tipo);
        catalogo.add(p);
        return p;
    }

    public ParametroNormativo editar(int id, String nombre, String unidad, double min, double max, String tipo) {
        for (int i = 0; i < catalogo.size(); i++) {
            if (catalogo.get(i).idParametro() == id) {
                ParametroNormativo actualizado = new ParametroNormativo(id, nombre, unidad, min, max, tipo);
                catalogo.set(i, actualizado);
                return actualizado;
            }
        }
        return null;
    }

    public boolean eliminar(int id) {
        return catalogo.removeIf(p -> p.idParametro() == id);
    }
}
