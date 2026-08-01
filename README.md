# HydroSafe UTTT

Sistema de monitoreo de calidad del agua. Backend Java sin frameworks (cero
dependencias externas — compila y corre con solo el JDK), frontend Vue sin
build step. Panel Admin separado, tiempo real vía SSE, exportación real a
CSV/Excel/PDF, seguridad reforzada, página de Documentación, y un sistema de
diseño propio ("Cisterna") en vez del típico dashboard azul de SaaS.

## Estructura

```
src/main/java/mx/edu/uttt/hydrosafe/
├── Main.java / Config.java / Util.java
├── web/            Router, Json, JsonHttp, CsvWriter, MiniXlsx, MiniPdf, StaticFileHandler
├── auth/           Session, AccessManager, LoginController
├── usuarios/       Usuario + UsuarioService
├── parametros/     catálogo de 5 parámetros, editable por admin
├── lecturas/       registro (batch) + historial + emite eventos SSE
├── alertas/        motor de reglas (Critica / Media)
├── reportes/       resumen + exportación CSV/XLSX/PDF
├── configuracion/  config del sistema (incluye video/github para /docs)
├── admin/          CRUD de parametros/usuarios/config + auditoría
├── seguridad/      PasswordHasher (PBKDF2), RateLimiter
├── auditoria/      log de acciones de administración
├── sistema/        log técnico (arranques, errores, bloqueos)
├── eventos/        EventBus (SSE) + EventosController
├── semaforo/       historial del estado general por hora
└── documentos/     subir/descargar la presentación

src/main/resources/
├── public/js/util.js    auth, SSE, notificaciones, i18n, tema, iconos
└── vue/
    ├── layout.html                    shell + router por hash
    ├── inline-styles.css              sistema de diseño "Cisterna" completo
    ├── components/app-shell.vue       sidebar + topbar compartidos
    └── views/
        ├── login-page.vue
        ├── dashboard-page.vue          tanque-gauge + tiras + grafica en vivo
        ├── alertas-page.vue
        ├── reportes-page.vue
        ├── docs-page.vue
        ├── admin-parametros.vue
        ├── admin-usuarios.vue
        ├── admin-configuracion.vue
        └── admin-registros.vue
```

## Endpoints

**Público**: `/api/usuarios/login`, `/api/usuarios/logout`, `/api/parametros`, `/api/configuracion`, `/api/resumen-publico`

**Requiere sesión**: `/api/lecturas` (POST), `/api/lecturas/tiempo-real`,
`/api/lecturas/tiempo-real-min`, `/api/lecturas/historial`, `/api/alertas`,
`/api/reportes`, `/api/reportes/exportar`, `/api/eventos` (SSE),
`/api/semaforo/historial-semana`, `/api/documentos/presentacion` (+ `/descargar`)

**Solo admin**: `/api/admin/parametros` (+ `/editar`, `/eliminar`),
`/api/admin/usuarios` (+ `/eliminar`), `/api/admin/configuracion`,
`/api/admin/auditoria`, `/api/admin/eventos-sistema`,
`/api/admin/documentos/presentacion`

## Siguiente paso natural

Conectar Firebird real donde están los TODOs. La tabla de auditoría, el log
de eventos y el historial del semáforo también son buenos candidatos para
persistir — ahora mismo viven en memoria y se pierden al reiniciar.

