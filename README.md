# HydroSafe UTTT

Sistema de monitoreo de calidad del agua. Backend Java sin frameworks (cero
dependencias externas — compila y corre con solo el JDK), frontend Vue sin
build step. Panel Admin separado, tiempo real vía SSE, exportación real a
CSV/Excel/PDF, seguridad reforzada, página de Documentación, y un sistema de
diseño propio ("Cisterna") en vez del típico dashboard azul de SaaS.

## Credenciales de prueba

- Monitor: `ing.mendoza@uttt.edu.mx` / `hydrosafe2026`
- Admin:   `admin@uttt.edu.mx` / `admin2026`

## Correr

```bash
cd HydroSafe
mkdir -p out
javac -encoding UTF-8 -d out $(find src/main/java -name "*.java")
cp -r src/main/resources/* out/
java -cp out mx.edu.uttt.hydrosafe.Main
```

Abre `http://localhost:7000`. En Garuda, si falta el JDK: `sudo pacman -S jdk-openjdk`.

## PWA + Modo kiosko

**Instalable en el celular** — abre `http://<ip-de-tu-compu>:7000` desde el
navegador del celular (misma red WiFi) y usa "Agregar a pantalla de inicio"
(Android/Chrome lo ofrece solo; en iOS es Compartir → Agregar a inicio). Abre
como app, sin barra de navegador, con su propio ícono.

- `manifest.json` con los íconos de la marca (192/512, más versión
  "maskable" para Android) — se generaron renderizando el mismo SVG de la
  gota que usa el resto de la app, no son aproximaciones a mano.
- `sw.js` (service worker) cachea el shell (HTML/CSS/JS/vistas) para que
  abra rápido, **pero nunca cachea `/api/*`**. Esto es deliberado y no
  negociable: si cacheara respuestas de la API, alguien podría abrir la app
  sin internet y ver una lectura de agua de hace días como si fuera la
  actual — inaceptable para un sistema que dice si el agua es segura. Mejor
  que falle visiblemente a que muestre un dato viejo con cara de actual. Lo
  verifiqué de verdad: inspeccioné el cache del navegador después de usar la
  app y confirmé que `/api/*` nunca aparece ahí.

**Modo kiosko** — botón nuevo en la barra superior (junto a tema/idioma).
Pide pantalla completa real (Fullscreen API) y un Wake Lock para que el
monitor no se apague solo mientras está de exhibición en el laboratorio;
oculta sidebar/topbar y agranda las lecturas para que se lean desde lejos.
Un botón discreto abajo a la derecha (semi-transparente hasta que le pasas
el mouse) para salir; si sales con ESC en vez del botón, el estado se
sincroniza solo. Probado de punta a punta con Playwright: activa, oculta el
sidebar, aparece el botón de salida, y al salir todo vuelve a su lugar.

## El rediseño ("Cisterna")

En vez del dashboard azul-SaaS genérico, la identidad visual está anclada en
lo que el sistema realmente vigila: una cisterna de agua, tubería de cobre
(que se oxida a verdigris — literal uno de los parámetros medidos),
sedimento mineral, papel de laboratorio.

- **Color**: verdigris (`--cobre`) como acento primario, ocre mineral
  (`--sedimento`) para advertencia, rojo "marea roja" (`--marea`) para
  crítico — nada de azul/ámbar/rojo genéricos de librería de íconos.
- **Tipografía**: Fraunces (serif con carácter) para títulos, IBM Plex Mono
  para todas las lecturas numéricas (como un instrumento de laboratorio),
  Inter para el resto de la UI.
- **El elemento central del Dashboard ya no es un semáforo genérico**: es un
  tanque de cisterna en SVG con agua animada (oleaje continuo, burbujas) cuyo
  color cambia según el estado general — verdigris si todo está bien, ocre en
  precaución, rojo en riesgo. No representa nivel de agua (esto mide calidad,
  no cantidad), solo claridad/color, para no dar una lectura falsa.
- **Las tarjetas de parámetros son "tiras reactivas"**: una franja de color
  arriba como una tira de prueba química que acaba de reaccionar, lectura en
  monoespaciada grande, como un LCD de instrumento.
- **Modo oscuro no es solo invertir colores**: es su propio concepto
  ("cisterna de noche" — tinta profunda por todo el fondo, no nada más el
  sidebar) con su propia paleta calculada, no una simple inversión.
- **El sidebar es un "riel de instrumentos"**: siempre tinta oscura sin
  importar el tema, como el bisel físico de un panel de control.
- **El login muestra un resumen público antes de entrar**: un panel junto al
  formulario con el estado general y los 5 parámetros actuales, para que
  cualquiera parado frente a la pantalla vea el estado del agua sin necesidad
  de iniciar sesión — como un tablero físico de laboratorio. Sale de un
  endpoint nuevo y público, `GET /api/resumen-publico`, que no expone nada
  sensible (nomás valores actuales y su estado).

Todo esto lo verifiqué con capturas de pantalla reales (Playwright +
Chromium headless) antes de mandártelo — login, dashboard en claro y oscuro,
zoom al tanque y a las tiras, alertas, reportes, admin, documentación, y
vista móvil (390px) — no me quedé con "debería verse bien".

**Nota**: como esto corre sobre com.sun.net.httpserver plano (sin bundler),
las tipografías (Fraunces / IBM Plex Mono / Inter) se cargan desde Google
Fonts vía CDN en `layout.html`, igual que Vue y vue3-sfc-loader se cargan
desde unpkg — necesitas conexión a internet la primera vez que lo abras en el
navegador (después el navegador las cachea).

## Qué se agregó en la vuelta anterior (funcionalidad)

**Tiempo real (SSE)** — `/api/eventos` deja la conexión abierta y transmite
cada lectura y alerta nueva en cuanto se registran. El Dashboard se actualiza
solo, con destello en las tarjetas y gráfica en vivo de los últimos 60 min.

**Notificaciones del navegador** — alertas críticas nuevas disparan
notificación del sistema operativo.

**Exportación real** — CSV, XLSX y PDF de verdad desde Reportes. Sin Apache
POI/iText (no hay acceso a Maven Central en el entorno donde armé esto):
XLSX es un ZIP de XML hecho a mano (`web/MiniXlsx.java`), PDF se arma a mano
con su tabla de referencias (`web/MiniPdf.java`). Los probé leyendo el XLSX
con `openpyxl` y el PDF con `pypdf` antes de mandártelos.

**Seguridad** — contraseñas con hash PBKDF2-HMAC-SHA256 (no BCrypt exacto,
pero misma familia, recomendado por NIST SP 800-63B como alternativa cuando
no tienes esa librería). Rate limiting: 5 intentos fallidos bloquean la
cuenta 60 segundos.

**Auditoría y logs** — panel Admin → Registros con auditoría de acciones
administrativas y log técnico del sistema.

**Estado histórico del semáforo** — cuántas horas estuvo el sistema en cada
nivel esta semana.

**Español/English** — toggle para el "chrome" de la app.

**Documentación (`/docs`)** — manual de uso, subida de presentación
(funcional), campos para video demo y repo de GitHub.

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

Token en header: `Authorization: Bearer <token>`. Excepción: `/api/eventos`
(SSE) acepta `?token=` en la query string porque `EventSource` no puede
mandar headers personalizados desde el navegador.

## Siguiente paso natural

Conectar Firebird real donde están los TODOs. La tabla de auditoría, el log
de eventos y el historial del semáforo también son buenos candidatos para
persistir — ahora mismo viven en memoria y se pierden al reiniciar.

