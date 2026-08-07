/**
 * HydroSafe (UTTT) — Sistema de Monitoreo de la Calidad del Agua
 * 
 * Autores:
 *   - Maria Fernanda Aldana Jiménez
 *   - Natali Isabel Chávez Alpízar
 *   - Hiyadir Raúlciel Barrera Cuervo
 * 
 * Universidad Tecnológica de Tula-Tepeji
 * Programa Educativo: Ingeniería en Tecnologías de la Información, área Infraestructura de Redes Inteligentes y Ciberseguridad
 * Empresa: Universidad Tecnológica de Tula-Tepeji
 * 
 * Asesor Académico: M. en C. Odisey Yasmin Porras Beltrán
 * Asesores Colaboradores: Marisol Reséndiz Vega, Mario Herrera Telles
 * 
 * Este software fue desarrollado durante el cuatrimestre mayo-agosto 2026.
 * Los derechos morales pertenecen a sus autores.
 * Queda prohibida la eliminación de los créditos originales y el uso o modificación del código sin autorización de los autores.
 */
// Utilidades compartidas del cliente HydroSafe.

const HydroSafeAuth = {
  TOKEN_KEY: "hydrosafe_token",
  USUARIO_KEY: "hydrosafe_usuario",
  ROL_KEY: "hydrosafe_rol",

  guardarSesion(token, correo, nombre, rol) {
    localStorage.setItem(this.TOKEN_KEY, token);
    localStorage.setItem(this.USUARIO_KEY, nombre || correo);
    localStorage.setItem(this.ROL_KEY, rol);
  },

  getToken() { return localStorage.getItem(this.TOKEN_KEY); },
  getUsuario() { return localStorage.getItem(this.USUARIO_KEY); },
  getRol() { return localStorage.getItem(this.ROL_KEY); },
  esAdmin() { return this.getRol() === "admin"; },

  cerrarSesion() {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USUARIO_KEY);
    localStorage.removeItem(this.ROL_KEY);
    window.location.hash = "#/login";
  },

  async llamarApi(path, options) {
    options = options || {};
    const token = this.getToken();
    const headers = Object.assign(
      { "Content-Type": "application/json" },
      options.headers || {},
      token ? { Authorization: "Bearer " + token } : {}
    );
    const res = await fetch("/api" + path, Object.assign({}, options, { headers: headers }));
    if (res.status === 401) {
      this.cerrarSesion();
      throw new Error("Sesion expirada");
    }
    if (!res.ok) {
      const errText = await res.text();
      throw new Error("Error " + res.status + ": " + errText);
    }
    return res.status === 204 ? null : res.json();
  },
};

// Iconos SVG reusables (para no repetir markup en cada .vue)
const HydroSafeIconos = {
  gota: '<svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 2s-6 8-6 13a6 6 0 0 0 12 0c0-5-6-13-6-13z"/></svg>',
  dashboard: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/></svg>',
  bell: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9"/><path d="M10.3 21a1.94 1.94 0 0 0 3.4 0"/></svg>',
  doc: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>',
  gear: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 1 1-4 0v-.09a1.65 1.65 0 0 0-1-1.51 1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 1 1 0-4h.09a1.65 1.65 0 0 0 1.51-1 1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 1 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 1 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"/></svg>',
  users: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>',
  wifi: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12.55a11 11 0 0 1 14.08 0"/><path d="M1.42 9a16 16 0 0 1 21.16 0"/><path d="M8.53 16.11a6 6 0 0 1 6.95 0"/><line x1="12" y1="20" x2="12.01" y2="20"/></svg>',
  chip: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="4" y="4" width="16" height="16" rx="2"/><rect x="9" y="9" width="6" height="6"/><line x1="9" y1="1" x2="9" y2="4"/><line x1="15" y1="1" x2="15" y2="4"/><line x1="9" y1="20" x2="9" y2="23"/><line x1="15" y1="20" x2="15" y2="23"/><line x1="20" y1="9" x2="23" y2="9"/><line x1="20" y1="14" x2="23" y2="14"/><line x1="1" y1="9" x2="4" y2="9"/><line x1="1" y1="14" x2="4" y2="14"/></svg>',
  logout: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>',
  warning: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>',
  alert: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>',
  check: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>',
  info: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>',
  atom: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="1"/><path d="M20.2 20.2c2.04-2.03.02-7.36-4.5-11.9-4.54-4.52-9.87-6.54-11.9-4.5-2.04 2.03-.02 7.36 4.5 11.9 4.54 4.52 9.87 6.54 11.9 4.5Z"/><path d="M15.7 15.7c4.52-4.54 6.54-9.87 4.5-11.9-2.03-2.04-7.36-.02-11.9 4.5-4.52 4.54-6.54 9.87-4.5 11.9 2.03 2.04 7.36.02 11.9-4.5Z"/></svg>',
  droplet: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2.69l5.66 5.66a8 8 0 1 1-11.31 0z"/></svg>',
  layers: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="12 2 2 7 12 12 22 7 12 2"/><polyline points="2 17 12 22 22 17"/><polyline points="2 12 12 17 22 12"/></svg>',
  bacteria: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="9"/><circle cx="8" cy="10" r="1.5"/><circle cx="15" cy="8" r="1"/><circle cx="14" cy="14" r="1.2"/><circle cx="9" cy="15" r="0.8"/></svg>',
  edit: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>',
  trash: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6"/><path d="M14 11v6"/></svg>',
  plus: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>',
  download: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>',
  print: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="6 9 6 2 18 2 18 9"/><path d="M6 18H4a2 2 0 0 1-2-2v-5a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v5a2 2 0 0 1-2 2h-2"/><rect x="6" y="14" width="12" height="8"/></svg>',
  refresh: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="23 4 23 10 17 10"/><polyline points="1 20 1 14 7 14"/><path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/></svg>',
  bacteria2: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="3"/><path d="M11 5v3M11 14v3M5 11h3M14 11h3M6.8 6.8l2 2M15.2 15.2l2 2M6.8 15.2l2-2M15.2 6.8l2-2"/></svg>',
  expandir: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M8 3H5a2 2 0 0 0-2 2v3m18 0V5a2 2 0 0 0-2-2h-3m0 18h3a2 2 0 0 0 2-2v-3M3 16v3a2 2 0 0 0 2 2h3"/></svg>',
  comprimir: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M8 3v3a2 2 0 0 1-2 2H3m18 0h-3a2 2 0 0 1-2-2V3m0 18v-3a2 2 0 0 1 2-2h3M3 16h3a2 2 0 0 1 2 2v3"/></svg>',
};

// Server-Sent Events: una sola conexion compartida por toda la app.
const HydroSafeEventos = {
  _fuente: null,
  _listeners: {},

  conectar() {
    if (this._fuente) return this._fuente;
    const token = HydroSafeAuth.getToken();
    if (!token) return null;
    // EventSource no permite mandar headers, asi que el token va en la query string.
    this._fuente = new EventSource("/api/eventos?token=" + encodeURIComponent(token));
    ["lectura", "alerta", "conectado"].forEach(tipo => {
      this._fuente.addEventListener(tipo, ev => {
        let datos = {};
        try { datos = JSON.parse(ev.data); } catch (e) { /* ignorar */ }
        (this._listeners[tipo] || []).forEach(cb => cb(datos));
      });
    });
    this._fuente.onerror = () => { /* el navegador reintenta solo */ };
    return this._fuente;
  },

  on(tipo, callback) {
    if (!this._listeners[tipo]) this._listeners[tipo] = [];
    this._listeners[tipo].push(callback);
  },

  desconectar() {
    if (this._fuente) { this._fuente.close(); this._fuente = null; }
    this._listeners = {};
  },
};

// Notificaciones del navegador (Notifications API -- no cuesta nada, no requiere backend extra).
const HydroSafeNotificaciones = {
  async pedirPermiso() {
    if (!("Notification" in window)) return false;
    if (Notification.permission === "granted") return true;
    if (Notification.permission === "denied") return false;
    const resultado = await Notification.requestPermission();
    return resultado === "granted";
  },
  mostrar(titulo, opciones) {
    if (!("Notification" in window) || Notification.permission !== "granted") return;
    try { new Notification(titulo, opciones || {}); } catch (e) { /* ignorar */ }
  },
};

// Tema oscuro/claro. Se aplica sobre <html data-theme="...">. La preferencia
// se lee ANTES de montar Vue (ver layout.html) para no hacer flash claro/oscuro.
const HydroSafeTema = {
  KEY: "hydrosafe_tema",
  obtener() { return localStorage.getItem(this.KEY) || "claro"; },
  aplicar(tema) {
    document.documentElement.setAttribute("data-theme", tema);
    localStorage.setItem(this.KEY, tema);
  },
  alternar() {
    const nuevo = this.obtener() === "oscuro" ? "claro" : "oscuro";
    this.aplicar(nuevo);
    return nuevo;
  },
};

// i18n minimo: es/en para el "chrome" de la app (nav, botones, titulos,
// estados). Las descripciones de alertas que arma el backend se quedan en
// espanol por ahora -- traducirlas necesitaria i18n del lado del servidor,
// que queda fuera de este alcance.
const HydroSafeI18n = {
  KEY: "hydrosafe_idioma",
  dict: {
    es: {
      nav_dashboard: "Dashboard", nav_alertas: "Alertas", nav_reportes: "Reportes",
      nav_docs: "Documentación", nav_parametros: "Parámetros", nav_usuarios: "Usuarios",
      nav_config: "Configuración", nav_registros: "Registros", nav_monitoreo: "Monitoreo",
      nav_admin: "Administración", cerrar_sesion: "Cerrar sesión",
      sistema_activo: "Sistema activo", encargado: "Encargado de Monitoreo", administrador: "Administrador",
      buena: "Buena", precaucion: "Precaución", riesgo: "Riesgo",
      normal: "Normal", advertencia: "Advertencia", critico: "Crítico",
      generar_reporte: "Generar reporte", limpiar_filtros: "Limpiar filtros",
      guardar: "Guardar", cancelar: "Cancelar", eliminar: "Eliminar", editar: "Editar",
    },
    en: {
      nav_dashboard: "Dashboard", nav_alertas: "Alerts", nav_reportes: "Reports",
      nav_docs: "Documentation", nav_parametros: "Parameters", nav_usuarios: "Users",
      nav_config: "Settings", nav_registros: "Logs", nav_monitoreo: "Monitoring",
      nav_admin: "Administration", cerrar_sesion: "Sign out",
      sistema_activo: "System active", encargado: "Monitoring Officer", administrador: "Administrator",
      buena: "Good", precaucion: "Caution", riesgo: "Risk",
      normal: "Normal", advertencia: "Warning", critico: "Critical",
      generar_reporte: "Generate report", limpiar_filtros: "Clear filters",
      guardar: "Save", cancelar: "Cancel", eliminar: "Delete", editar: "Edit",
    },
  },
  obtener() { return localStorage.getItem(this.KEY) || "es"; },
  alternar() {
    const nuevo = this.obtener() === "es" ? "en" : "es";
    localStorage.setItem(this.KEY, nuevo);
    return nuevo;
  },
  t(clave) {
    const idioma = this.obtener();
    return (this.dict[idioma] && this.dict[idioma][clave]) || this.dict.es[clave] || clave;
  },
};

// Formateo compartido
const HydroSafeFormato = {
  fecha(f) {
    return f ? new Date(f).toLocaleString("es-MX", { day: "2-digit", month: "2-digit", year: "numeric", hour: "2-digit", minute: "2-digit" }) : "-";
  },
  fechaLarga() {
    const d = new Date();
    return d.toLocaleDateString("es-MX", { weekday: "long", day: "numeric", month: "long", year: "numeric" }) + " · " + d.toLocaleTimeString("es-MX", { hour: "2-digit", minute: "2-digit" });
  },
  hora(f) {
    return f ? new Date(f).toLocaleTimeString("es-MX", { hour: "2-digit", minute: "2-digit" }) : "-";
  },
  iniciales(nombre) {
    if (!nombre) return "?";
    return nombre.split(" ").filter(Boolean).slice(0, 2).map(w => w[0]).join("").toUpperCase();
  },
  haceCuanto(f) {
    if (!f) return "-";
    const diffMin = Math.round((Date.now() - new Date(f).getTime()) / 60000);
    if (diffMin < 1) return "Ahora";
    if (diffMin < 60) return "Hace " + diffMin + " min";
    const diffH = Math.round(diffMin / 60);
    if (diffH < 24) return "Hace " + diffH + " h";
    return "Hace " + Math.round(diffH / 24) + " d";
  }
};
