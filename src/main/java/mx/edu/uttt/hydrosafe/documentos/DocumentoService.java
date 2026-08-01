package mx.edu.uttt.hydrosafe.documentos;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;

/**
 * Guarda la presentacion (PPTX/PDF) que se sube desde /docs en disco, bajo
 * data/uploads/ (relativo al directorio de trabajo, igual que config/). Solo
 * se guarda 1 archivo a la vez: cada subida reemplaza la anterior.
 */
public class DocumentoService {

    private static final Path CARPETA = Paths.get("data", "uploads");

    private String nombreOriginal;
    private String rutaGuardada;
    private LocalDateTime subidoEn;

    public record InfoDocumento(String nombreOriginal, LocalDateTime subidoEn, boolean disponible) {}

    public InfoDocumento info() {
        return new InfoDocumento(nombreOriginal, subidoEn, rutaGuardada != null);
    }

    public void guardar(String nombreOriginal, byte[] contenido) throws IOException {
        Files.createDirectories(CARPETA);
        String extension = nombreOriginal.contains(".") ? nombreOriginal.substring(nombreOriginal.lastIndexOf('.')) : "";
        Path destino = CARPETA.resolve("presentacion" + extension);
        Files.write(destino, contenido, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        this.nombreOriginal = nombreOriginal;
        this.rutaGuardada = destino.toString();
        this.subidoEn = LocalDateTime.now();
    }

    public byte[] leer() throws IOException {
        if (rutaGuardada == null) return null;
        return Files.readAllBytes(Paths.get(rutaGuardada));
    }

    public String nombreOriginal() { return nombreOriginal; }
}
