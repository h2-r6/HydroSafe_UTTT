package mx.edu.uttt.hydrosafe;

import com.sun.net.httpserver.HttpServer;
import mx.edu.uttt.hydrosafe.admin.AdminController;
import mx.edu.uttt.hydrosafe.alertas.AlertaController;
import mx.edu.uttt.hydrosafe.alertas.AlertaService;
import mx.edu.uttt.hydrosafe.auditoria.AuditoriaService;
import mx.edu.uttt.hydrosafe.auth.AccessManager;
import mx.edu.uttt.hydrosafe.auth.LoginController;
import mx.edu.uttt.hydrosafe.configuracion.ConfiguracionController;
import mx.edu.uttt.hydrosafe.configuracion.ConfiguracionService;
import mx.edu.uttt.hydrosafe.documentos.DocumentoController;
import mx.edu.uttt.hydrosafe.documentos.DocumentoService;
import mx.edu.uttt.hydrosafe.eventos.EventBus;
import mx.edu.uttt.hydrosafe.eventos.EventosController;
import mx.edu.uttt.hydrosafe.lecturas.LecturaController;
import mx.edu.uttt.hydrosafe.lecturas.LecturaService;
import mx.edu.uttt.hydrosafe.parametros.ParametroController;
import mx.edu.uttt.hydrosafe.parametros.ParametroService;
import mx.edu.uttt.hydrosafe.reportes.ReporteController;
import mx.edu.uttt.hydrosafe.reportes.ReporteExportController;
import mx.edu.uttt.hydrosafe.reportes.ReporteService;
import mx.edu.uttt.hydrosafe.resumen.ResumenPublicoController;
import mx.edu.uttt.hydrosafe.semaforo.SemaforoController;
import mx.edu.uttt.hydrosafe.semaforo.SemaforoService;
import mx.edu.uttt.hydrosafe.sistema.EventoSistemaService;
import mx.edu.uttt.hydrosafe.usuarios.UsuarioService;
import mx.edu.uttt.hydrosafe.web.Router;
import mx.edu.uttt.hydrosafe.web.StaticFileHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) throws IOException {
        // --- Infra compartida ---
        EventBus eventBus = new EventBus();
        AuditoriaService auditoriaService = new AuditoriaService();
        EventoSistemaService eventoSistemaService = new EventoSistemaService();

        // --- Servicios (en memoria; ver TODOs para conectar Firebird) ---
        ParametroService parametroService = new ParametroService();
        AlertaService alertaService = new AlertaService(parametroService);
        SemaforoService semaforoService = new SemaforoService();
        semaforoService.sembrarDemo();
        LecturaService lecturaService = new LecturaService(parametroService, alertaService, semaforoService, eventBus);
        ReporteService reporteService = new ReporteService(lecturaService, alertaService);
        UsuarioService usuarioService = new UsuarioService();
        ConfiguracionService configuracionService = new ConfiguracionService();
        DocumentoService documentoService = new DocumentoService();

        // --- Controllers ---
        LoginController loginController = new LoginController(usuarioService, eventoSistemaService);
        ParametroController parametroController = new ParametroController(parametroService);
        LecturaController lecturaController = new LecturaController(lecturaService);
        AlertaController alertaController = new AlertaController(alertaService);
        ReporteController reporteController = new ReporteController(reporteService);
        ReporteExportController reporteExportController = new ReporteExportController(reporteService, parametroService);
        ConfiguracionController configuracionController = new ConfiguracionController(configuracionService);
        AdminController adminController = new AdminController(parametroService, usuarioService, configuracionService, auditoriaService, eventoSistemaService);
        EventosController eventosController = new EventosController(eventBus);
        SemaforoController semaforoController = new SemaforoController(semaforoService);
        DocumentoController documentoController = new DocumentoController(documentoService);
        ResumenPublicoController resumenPublicoController = new ResumenPublicoController(parametroService, lecturaService, configuracionService);

        Router router = new Router();

        // ---------- Publico ----------
        router.post("/api/usuarios/login", loginController::login);
        router.post("/api/usuarios/logout", loginController::logout);
        router.get("/api/parametros", parametroController::listar);
        router.get("/api/configuracion", configuracionController::obtener);
        router.get("/api/resumen-publico", resumenPublicoController::obtener);

        // ---------- Requiere sesion (monitor o admin) ----------
        router.post("/api/lecturas", AccessManager.proteger(lecturaController::registrar));
        router.get("/api/lecturas/tiempo-real", AccessManager.proteger(lecturaController::tiempoReal));
        router.get("/api/lecturas/tiempo-real-min", AccessManager.proteger(lecturaController::ultimosMinutos));
        router.get("/api/lecturas/historial", AccessManager.proteger(lecturaController::historial));
        router.get("/api/alertas", AccessManager.proteger(alertaController::listar));
        router.get("/api/reportes", AccessManager.proteger(reporteController::generar));
        router.get("/api/reportes/exportar", AccessManager.proteger(reporteExportController::exportar));
        router.get("/api/eventos", AccessManager.proteger(eventosController::suscribir));
        router.get("/api/semaforo/historial-semana", AccessManager.proteger(semaforoController::horasSemana));
        router.get("/api/documentos/presentacion", AccessManager.proteger(documentoController::info));
        router.get("/api/documentos/presentacion/descargar", AccessManager.proteger(documentoController::descargar));

        // ---------- Solo admin ----------
        router.post("/api/admin/parametros", AccessManager.protegerAdmin(adminController::crearParametro));
        router.post("/api/admin/parametros/editar", AccessManager.protegerAdmin(adminController::editarParametro));
        router.post("/api/admin/parametros/eliminar", AccessManager.protegerAdmin(adminController::eliminarParametro));
        router.get("/api/admin/usuarios", AccessManager.protegerAdmin(adminController::listarUsuarios));
        router.post("/api/admin/usuarios", AccessManager.protegerAdmin(adminController::crearUsuario));
        router.post("/api/admin/usuarios/eliminar", AccessManager.protegerAdmin(adminController::eliminarUsuario));
        router.get("/api/admin/configuracion", AccessManager.protegerAdmin(adminController::obtenerConfig));
        router.post("/api/admin/configuracion", AccessManager.protegerAdmin(adminController::actualizarConfig));
        router.get("/api/admin/auditoria", AccessManager.protegerAdmin(adminController::listarAuditoria));
        router.get("/api/admin/eventos-sistema", AccessManager.protegerAdmin(adminController::listarEventosSistema));
        router.post("/api/admin/documentos/presentacion", AccessManager.protegerAdmin(documentoController::subir));

        // ---------- Estaticos (Vue sin bundler) ----------
        router.archivosEstaticos(new StaticFileHandler()::servir);

        int puerto = Config.getPort();
        HttpServer server = HttpServer.create(new InetSocketAddress(puerto), 0);
        // Cached (no fixed) porque cada conexion SSE abierta (/api/eventos) ocupa
        // un hilo mientras el usuario tenga el dashboard abierto; con un pool fijo
        // chico, varias pestanas abiertas podrian dejar sin hilos a la API normal.
        server.setExecutor(Executors.newCachedThreadPool());
        router.montarEn(server);
        server.start();

        eventoSistemaService.registrar("arranque", "HydroSafe iniciado en el puerto " + puerto);

        System.out.println(Config.getAppName() + " corriendo en http://localhost:" + puerto);
        System.out.println("Monitor -> ing.mendoza@uttt.edu.mx / hydrosafe2026");
        System.out.println("Admin   -> admin@uttt.edu.mx / admin2026");
    }
}
