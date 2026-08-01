package mx.edu.uttt.hydrosafe.usuarios;

import mx.edu.uttt.hydrosafe.seguridad.PasswordHasher;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * CRUD de usuarios. Las contrasenas se guardan como hash PBKDF2 (ver
 * PasswordHasher), nunca en texto plano -- ni siquiera las de prueba.
 * TODO: reemplazar por tabla ENCARGADO_MONITOREO/USUARIO en Firebird.
 *
 * Roles:
 *   - "monitor": el Encargado de Monitoreo del PDF (dashboard, alertas, reportes)
 *   - "admin":   puede lo del monitor + editar parametros/usuarios/config del sistema
 */
public class UsuarioService {

    private final List<Usuario> usuarios = new ArrayList<>();
    private final AtomicInteger contador = new AtomicInteger(1);

    public UsuarioService() {
        crear("ing.mendoza@uttt.edu.mx", "hydrosafe2026", "Ing. Carlos Mendoza", "monitor");
        crear("admin@uttt.edu.mx", "admin2026", "Administrador HydroSafe", "admin");
    }

    public List<Usuario> listar() {
        return List.copyOf(usuarios);
    }

    public Usuario autenticar(String correo, String contrasenaPlano) {
        return usuarios.stream()
                .filter(u -> u.correo().equalsIgnoreCase(correo))
                .filter(u -> PasswordHasher.verificar(contrasenaPlano, u.contrasena()))
                .findFirst().orElse(null);
    }

    public Usuario crear(String correo, String contrasenaPlano, String nombre, String rol) {
        Usuario u = new Usuario(contador.getAndIncrement(), correo, PasswordHasher.hash(contrasenaPlano), nombre, rol);
        usuarios.add(u);
        return u;
    }

    public boolean eliminar(int id) {
        return usuarios.removeIf(u -> u.idUsuario() == id);
    }
}
