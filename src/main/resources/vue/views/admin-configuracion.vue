<template>
  <app-shell vista="admin/configuracion">
    <h1 class="page-title animate-in">Configuración del sistema</h1>
    <p class="page-subtitle animate-in">Ajusta la información pública del nodo IoT y la app (visible en el dashboard y el header).</p>

    <div v-if="cargando" class="loading-state">
      <div class="loading-wave"><span></span><span></span><span></span></div>
      <p class="loading-text">Cargando configuración...</p>
    </div>

    <template v-else>
      <div class="admin-panel animate-in-1">
        <div class="admin-panel-header">
          <div>
            <div class="admin-panel-title">Identidad de la aplicación</div>
            <div class="admin-panel-desc">Estos campos aparecen en el sidebar y el login.</div>
          </div>
        </div>
        <div class="form-grid">
          <div class="form-field">
            <label>Nombre de la app</label>
            <input v-model="config.nombreApp" />
          </div>
          <div class="form-field">
            <label>Versión</label>
            <input v-model="config.version" placeholder="v2.4.1" />
          </div>
        </div>
      </div>

      <div class="admin-panel animate-in-2" style="margin-top: 18px;">
        <div class="admin-panel-header">
          <div>
            <div class="admin-panel-title">Nodo IoT actual</div>
            <div class="admin-panel-desc">Datos del ESP32 físico conectado a la red del laboratorio.</div>
          </div>
        </div>
        <div class="form-grid">
          <div class="form-field">
            <label>Ubicación del nodo</label>
            <input v-model="config.ubicacionNodo" placeholder="Cisterna Principal" />
          </div>
          <div class="form-field">
            <label>Nodo ID</label>
            <input v-model="config.nodeId" placeholder="CISTERNA-01" />
          </div>
          <div class="form-field">
            <label>Microcontrolador</label>
            <input v-model="config.microcontrolador" placeholder="ESP32" />
          </div>
          <div class="form-field">
            <label>SSID Wi-Fi</label>
            <input v-model="config.wifiSSID" placeholder="UTTTLabIoT" />
          </div>
          <div class="form-field">
            <label>RSSI (dBm)</label>
            <input type="number" v-model.number="config.wifiRSSI" />
          </div>
          <div class="form-field">
            <label>Encargado responsable</label>
            <input v-model="config.encargadoResponsable" placeholder="Ing. Carlos Mendoza" />
          </div>
        </div>
      </div>

      <div class="admin-panel animate-in-2" style="margin-top: 18px;">
        <div class="admin-panel-header">
          <div>
            <div class="admin-panel-title">Documentación pública</div>
            <div class="admin-panel-desc">Estos links aparecen en la página de Documentación (/docs). Déjalos vacíos si aún no los tienes.</div>
          </div>
        </div>
        <div class="form-grid">
          <div class="form-field">
            <label>URL del video demo (YouTube, etc.)</label>
            <input v-model="config.videoUrl" placeholder="https://www.youtube.com/watch?v=..." />
          </div>
          <div class="form-field">
            <label>URL del repositorio (GitHub)</label>
            <input v-model="config.githubUrl" placeholder="https://github.com/usuario/hydrosafe" />
          </div>
        </div>
      </div>

      <div style="display:flex; gap:10px; justify-content:flex-end; margin-top: 18px;">
        <button class="btn-secondary" @click="cargar">Descartar cambios</button>
        <button class="btn-primary" @click="guardar" :disabled="guardando">
          {{ guardando ? "Guardando..." : "Guardar configuración" }}
        </button>
      </div>
      <p v-if="mensaje" style="margin-top:12px; color:var(--hs-verde); font-size:13px; text-align:right;">✓ {{ mensaje }}</p>
    </template>
  </app-shell>
</template>

<script>
import AppShell from "/vue/components/app-shell.vue";

export default {
  components: { AppShell },
  data() {
    return { config: {}, cargando: true, guardando: false, mensaje: "" };
  },
  async created() {
    if (!HydroSafeAuth.esAdmin()) { window.location.hash = "#/dashboard"; return; }
    await this.cargar();
  },
  methods: {
    async cargar() {
      this.cargando = true;
      try { this.config = await HydroSafeAuth.llamarApi("/admin/configuracion"); }
      finally { this.cargando = false; }
    },
    async guardar() {
      this.guardando = true; this.mensaje = "";
      try {
        await HydroSafeAuth.llamarApi("/admin/configuracion", { method: "POST", body: JSON.stringify(this.config) });
        this.mensaje = "Configuración actualizada.";
        setTimeout(() => this.mensaje = "", 3000);
      } finally { this.guardando = false; }
    },
  },
};
</script>
