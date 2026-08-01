package mx.edu.uttt.hydrosafe.admin;

import com.sun.net.httpserver.HttpExchange;
import mx.edu.uttt.hydrosafe.auditoria.AuditoriaService;
import mx.edu.uttt.hydrosafe.auth.AccessManager;
import mx.edu.uttt.hydrosafe.auth.Session;
import mx.edu.uttt.hydrosafe.configuracion.Configuracion;
import mx.edu.uttt.hydrosafe.configuracion.ConfiguracionService;
import mx.edu.uttt.hydrosafe.parametros.ParametroNormativo;
import mx.edu.uttt.hydrosafe.parametros.ParametroService;
import mx.edu.uttt.hydrosafe.sistema.EventoSistemaService;
import mx.edu.uttt.hydrosafe.usuarios.Usuario;
import mx.edu.uttt.hydrosafe.usuarios.UsuarioService;
import mx.edu.uttt.hydrosafe.web.JsonHttp;

import java.io.IOException;
import java.util.Map;

/** Endpoints del panel de administracion. Todos van envueltos con AccessManager.protegerAdmin(). */
public class AdminController {

    private final ParametroService parametroService;
    private final UsuarioService usuarioService;
    private final ConfiguracionService configuracionService;
    private final AuditoriaService auditoriaService;
    private final EventoSistemaService eventoSistemaService;

    public AdminController(ParametroService p, UsuarioService u, ConfiguracionService c,
                            AuditoriaService auditoria, EventoSistemaService eventos) {
        this.parametroService = p;
        this.usuarioService = u;
        this.configuracionService = c;
        this.auditoriaService = auditoria;
        this.eventoSistemaService = eventos;
    }

    private String quienHizoEsto(HttpExchange e) {
        Session s = AccessManager.sesionDesde(e);
        return s != null ? s.nombre : "desconocido";
    }

    // ---------- Parametros ----------
    public record ParametroDTO(int idParametro, String nombreParametro, String unidadMedida,
                               double limiteMin, double limiteMax, String tipo) {}

    public void crearParametro(HttpExchange e) throws IOException {
        ParametroDTO dto = JsonHttp.leerCuerpo(e, ParametroDTO.class);
        ParametroNormativo creado = parametroService.crear(
                dto.nombreParametro(), dto.unidadMedida(), dto.limiteMin(), dto.limiteMax(),
                dto.tipo() == null ? "concentracion" : dto.tipo());
        auditoriaService.registrar(quienHizoEsto(e), "crear_parametro", "Creó el parámetro \"" + creado.nombreParametro() + "\"");
        JsonHttp.enviar(e, 201, creado);
    }

    public void editarParametro(HttpExchange e) throws IOException {
        ParametroDTO dto = JsonHttp.leerCuerpo(e, ParametroDTO.class);
        ParametroNormativo actualizado = parametroService.editar(
                dto.idParametro(), dto.nombreParametro(), dto.unidadMedida(),
                dto.limiteMin(), dto.limiteMax(),
                dto.tipo() == null ? "concentracion" : dto.tipo());
        if (actualizado == null) { JsonHttp.enviarVacio(e, 404); return; }
        auditoriaService.registrar(quienHizoEsto(e), "editar_parametro",
                "Editó \"" + actualizado.nombreParametro() + "\" (límite: " + actualizado.limiteMin() + " - " + actualizado.limiteMax() + ")");
        JsonHttp.enviar(e, 200, actualizado);
    }

    public void eliminarParametro(HttpExchange e) throws IOException {
        Map<String, String> q = JsonHttp.queryParams(e);
        int id = Integer.parseInt(q.getOrDefault("id", "0"));
        boolean ok = parametroService.eliminar(id);
        if (ok) auditoriaService.registrar(quienHizoEsto(e), "eliminar_parametro", "Eliminó el parámetro #" + id);
        JsonHttp.enviar(e, 200, Map.of("eliminado", ok));
    }

    // ---------- Usuarios ----------
    public record UsuarioDTO(int idUsuario, String correo, String contrasena, String nombre, String rol) {}

    public void listarUsuarios(HttpExchange e) throws IOException {
        JsonHttp.enviar(e, 200, usuarioService.listar().stream()
                .map(u -> Map.of("idUsuario", u.idUsuario(), "correo", u.correo(), "nombre", u.nombre(), "rol", u.rol()))
                .toList());
    }

    public void crearUsuario(HttpExchange e) throws IOException {
        UsuarioDTO dto = JsonHttp.leerCuerpo(e, UsuarioDTO.class);
        Usuario nuevo = usuarioService.crear(dto.correo(), dto.contrasena(), dto.nombre(), dto.rol());
        auditoriaService.registrar(quienHizoEsto(e), "crear_usuario", "Dio de alta a " + nuevo.nombre() + " (" + nuevo.rol() + ")");
        JsonHttp.enviar(e, 201, Map.of("idUsuario", nuevo.idUsuario(), "correo", nuevo.correo(), "nombre", nuevo.nombre(), "rol", nuevo.rol()));
    }

    public void eliminarUsuario(HttpExchange e) throws IOException {
        Map<String, String> q = JsonHttp.queryParams(e);
        int id = Integer.parseInt(q.getOrDefault("id", "0"));
        boolean ok = usuarioService.eliminar(id);
        if (ok) auditoriaService.registrar(quienHizoEsto(e), "eliminar_usuario", "Eliminó al usuario #" + id);
        JsonHttp.enviar(e, 200, Map.of("eliminado", ok));
    }

    // ---------- Configuracion ----------
    public void obtenerConfig(HttpExchange e) throws IOException {
        JsonHttp.enviar(e, 200, configuracionService.obtener());
    }

    public void actualizarConfig(HttpExchange e) throws IOException {
        Configuracion c = JsonHttp.leerCuerpo(e, Configuracion.class);
        configuracionService.actualizar(c);
        String quien = quienHizoEsto(e);
        auditoriaService.registrar(quien, "actualizar_configuracion", "Actualizó la configuración del sistema");
        eventoSistemaService.registrar("config", "Configuración actualizada por " + quien);
        JsonHttp.enviar(e, 200, c);
    }

    // ---------- Registros (para el panel de admin) ----------
    public void listarAuditoria(HttpExchange e) throws IOException {
        JsonHttp.enviar(e, 200, auditoriaService.listar());
    }

    public void listarEventosSistema(HttpExchange e) throws IOException {
        JsonHttp.enviar(e, 200, eventoSistemaService.listar());
    }
}
