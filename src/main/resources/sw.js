// Service worker de HydroSafe.
//
// REGLA DE SEGURIDAD, NO NEGOCIABLE: nunca cachear nada bajo /api/ ni /sw.js.
// Esta app le dice a alguien si el agua es segura de tomar. Si cacheara
// respuestas de la API, alguien podria abrir la app sin internet y ver una
// lectura de hace 3 dias como si fuera la de ahorita -- eso es peor que no
// mostrar nada. Mejor que falle visiblemente a que mienta calladito.
//
// Lo que SI cachea (para que la app abra rapido / instalada como PWA):
// el layout, los estilos, los .vue de las vistas, y los iconos.

const CACHE_NAME = "hydrosafe-shell-v1";

const ARCHIVOS_SHELL = [
  "/",
  "/vue/layout.html",
  "/vue/inline-styles.css",
  "/public/js/util.js",
  "/vue/components/app-shell.vue",
  "/vue/views/login-page.vue",
  "/vue/views/dashboard-page.vue",
  "/vue/views/alertas-page.vue",
  "/vue/views/reportes-page.vue",
  "/vue/views/docs-page.vue",
  "/public/icons/icon-192.png",
  "/public/icons/icon-512.png",
];

self.addEventListener("install", (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then((cache) => cache.addAll(ARCHIVOS_SHELL))
      .catch(() => {}) // si algo no carga (ej. sin internet la primera vez), no truena la instalacion
  );
  self.skipWaiting();
});

self.addEventListener("activate", (event) => {
  event.waitUntil(
    caches.keys().then((nombres) =>
      Promise.all(nombres.filter((n) => n !== CACHE_NAME).map((n) => caches.delete(n)))
    )
  );
  self.clients.claim();
});

self.addEventListener("fetch", (event) => {
  const url = new URL(event.request.url);

  // Nunca intervenir la API, el stream de eventos SSE, ni el service worker mismo.
  if (url.pathname.startsWith("/api/") || url.pathname === "/sw.js") return;

  // Solo interceptar GET (POST/PUT no se cachean de todas formas)
  if (event.request.method !== "GET") return;

  // Solo mismo origen (no interceptar las fuentes/CDN externos)
  if (url.origin !== self.location.origin) return;

  event.respondWith(
    caches.match(event.request).then((cacheada) => {
      const desdeRed = fetch(event.request)
        .then((respuesta) => {
          if (respuesta && respuesta.ok) {
            const copia = respuesta.clone();
            caches.open(CACHE_NAME).then((cache) => cache.put(event.request, copia));
          }
          return respuesta;
        })
        .catch(() => cacheada);
      // Stale-while-revalidate: responde rapido con lo cacheado si existe,
      // y de pasada actualiza el cache en segundo plano para la proxima vez.
      return cacheada || desdeRed;
    })
  );
});
