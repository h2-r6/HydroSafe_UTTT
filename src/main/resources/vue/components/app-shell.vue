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
<template>
  <div class="app-shell" :class="{ 'modo-kiosko': kiosko }">
    <aside class="sidebar">
      <div class="sidebar-brand">
        <div class="brand-drop">
          <svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 2s-6 8-6 13a6 6 0 0 0 12 0c0-5-6-13-6-13z"/></svg>
        </div>
        <div class="brand-text">
          <strong>{{ config.nombreApp || "HydroSafe" }}</strong>
          <span>UTTT · {{ config.version || "v2.4.1" }}</span>
        </div>
      </div>

      <nav>
        <template v-if="!esAdmin">
          <a href="#/dashboard" class="nav-item" :class="{ active: vista === 'dashboard' }" v-html="iconoConTexto('dashboard', i18n.t('nav_dashboard'))"></a>
          <a href="#/alertas" class="nav-item" :class="{ active: vista === 'alertas' }" v-html="iconoConTexto('bell', i18n.t('nav_alertas'))"></a>
          <a href="#/reportes" class="nav-item" :class="{ active: vista === 'reportes' }" v-html="iconoConTexto('doc', i18n.t('nav_reportes'))"></a>
          <a href="#/docs" class="nav-item" :class="{ active: vista === 'docs' }" v-html="iconoConTexto('info', i18n.t('nav_docs'))"></a>
        </template>

        <template v-else>
          <div class="nav-section-title">{{ i18n.t('nav_monitoreo') }}</div>
          <a href="#/dashboard" class="nav-item" :class="{ active: vista === 'dashboard' }" v-html="iconoConTexto('dashboard', i18n.t('nav_dashboard'))"></a>
          <a href="#/alertas" class="nav-item" :class="{ active: vista === 'alertas' }" v-html="iconoConTexto('bell', i18n.t('nav_alertas'))"></a>
          <a href="#/reportes" class="nav-item" :class="{ active: vista === 'reportes' }" v-html="iconoConTexto('doc', i18n.t('nav_reportes'))"></a>
          <a href="#/docs" class="nav-item" :class="{ active: vista === 'docs' }" v-html="iconoConTexto('info', i18n.t('nav_docs'))"></a>
          <div class="nav-section-title">{{ i18n.t('nav_admin') }}</div>
          <a href="#/admin/parametros" class="nav-item" :class="{ active: vista === 'admin/parametros' }" v-html="iconoConTexto('atom', i18n.t('nav_parametros'))"></a>
          <a href="#/admin/usuarios" class="nav-item" :class="{ active: vista === 'admin/usuarios' }" v-html="iconoConTexto('users', i18n.t('nav_usuarios'))"></a>
          <a href="#/admin/configuracion" class="nav-item" :class="{ active: vista === 'admin/configuracion' }" v-html="iconoConTexto('gear', i18n.t('nav_config'))"></a>
          <a href="#/admin/registros" class="nav-item" :class="{ active: vista === 'admin/registros' }" v-html="iconoConTexto('layers', i18n.t('nav_registros'))"></a>
        </template>
      </nav>

      <div class="sidebar-footer">
        <button class="btn-ghost" @click="salir" v-html="iconoConTexto('logout', i18n.t('cerrar_sesion'))"></button>
      </div>
    </aside>

    <main class="content">
      <header class="topbar animate-in">
        <div class="topbar-left">
          <span class="status-pill">{{ i18n.t('sistema_activo') }}</span>
          <span class="topbar-date">{{ fechaLarga }}</span>
        </div>
        <div class="topbar-toggles">
          <button class="toggle-btn" @click="alternarTema" title="Cambiar tema">
            <svg v-if="tema === 'oscuro'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="5"/><line x1="12" y1="1" x2="12" y2="3"/><line x1="12" y1="21" x2="12" y2="23"/><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"/><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"/><line x1="1" y1="12" x2="3" y2="12"/><line x1="21" y1="12" x2="23" y2="12"/><line x1="4.22" y1="19.78" x2="5.64" y2="18.36"/><line x1="18.36" y1="5.64" x2="19.78" y2="4.22"/></svg>
            <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/></svg>
          </button>
          <button class="toggle-btn toggle-lang" @click="alternarIdioma" title="Switch language">{{ idioma.toUpperCase() }}</button>
          <button class="toggle-btn" @click="alternarKiosko" title="Modo kiosko (pantalla completa)">
            <span v-html="iconos.expandir"></span>
          </button>
        </div>
        <div class="topbar-user">
          <div class="avatar">{{ iniciales }}</div>
          <div>
            <strong>{{ usuario }}</strong>
            <span>{{ esAdmin ? i18n.t('administrador') : i18n.t('encargado') }} · UTTT</span>
          </div>
        </div>
      </header>
      <slot></slot>
    </main>

    <button v-if="kiosko" class="kiosko-salir" @click="salirKiosko" v-html="iconoConTexto('comprimir', 'Salir de modo kiosko')"></button>
  </div>
</template>

<script>
export default {
  props: { vista: { type: String, required: true } },
  data() {
    return {
      usuario: HydroSafeAuth.getUsuario() || "Usuario",
      esAdmin: HydroSafeAuth.esAdmin(),
      fechaLarga: HydroSafeFormato.fechaLarga(),
      config: {},
      relojInterval: null,
      tema: HydroSafeTema.obtener(),
      idioma: HydroSafeI18n.obtener(),
      i18n: HydroSafeI18n,
      iconos: HydroSafeIconos,
      kiosko: false,
      wakeLockRef: null,
    };
  },
  computed: {
    iniciales() { return HydroSafeFormato.iniciales(this.usuario); },
  },
  async created() {
    try { this.config = await fetch("/api/configuracion").then(r => r.json()); }
    catch (e) { /* ignora, no bloqueamos por esto */ }
    this.relojInterval = setInterval(() => { this.fechaLarga = HydroSafeFormato.fechaLarga(); }, 60000);
    document.addEventListener("fullscreenchange", this.alSalirFullscreen);
  },
  beforeUnmount() {
    if (this.relojInterval) clearInterval(this.relojInterval);
    document.removeEventListener("fullscreenchange", this.alSalirFullscreen);
  },
  methods: {
    iconoConTexto(icono, texto) {
      return HydroSafeIconos[icono] + '<span>' + texto + '</span>';
    },
    salir() { HydroSafeEventos.desconectar(); HydroSafeAuth.cerrarSesion(); },
    alternarTema() { this.tema = HydroSafeTema.alternar(); },
    alternarIdioma() { this.idioma = HydroSafeI18n.alternar(); window.location.reload(); },
    async alternarKiosko() {
      if (this.kiosko) { this.salirKiosko(); return; }
      try { await document.documentElement.requestFullscreen(); }
      catch (e) { /* algunos navegadores (ej. iOS Safari) no dejan pantalla completa real -- seguimos igual, solo sin eso */ }
      this.kiosko = true;
      this.pedirWakeLock();
    },
    async pedirWakeLock() {
      if (!("wakeLock" in navigator)) return;
      try { this.wakeLockRef = await navigator.wakeLock.request("screen"); }
      catch (e) { /* ignorar: por ejemplo si la pestana no esta visible al pedirlo */ }
    },
    liberarWakeLock() {
      if (this.wakeLockRef) { this.wakeLockRef.release().catch(() => {}); this.wakeLockRef = null; }
    },
    salirKiosko() {
      this.kiosko = false;
      if (document.fullscreenElement) document.exitFullscreen().catch(() => {});
      this.liberarWakeLock();
    },
    alSalirFullscreen() {
      // si el usuario sale con ESC (no con el boton), sincronizamos el estado
      if (!document.fullscreenElement && this.kiosko) {
        this.kiosko = false;
        this.liberarWakeLock();
      }
    },
  },
};
</script>
