package mx.edu.uttt.hydrosafe.usuarios;

/** Extiende ENCARGADO_MONITOREO del PDF con un campo "rol" para distinguir monitor vs admin. */
public record Usuario(
        int idUsuario,
        String correo,
        String contrasena,
        String nombre,
        String rol
) {}
