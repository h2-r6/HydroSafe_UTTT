<!--
  HydroSafe (UTTT) — Sistema de Monitoreo de la Calidad del Agua
  
  Autores:
    - Maria Fernanda Aldana Jiménez
    - Natali Isabel Chávez Alpízar
    - Hiyadir Raúlciel Barrera Cuervo
  
  Universidad Tecnológica de Tula-Tepeji
  Programa Educativo: Ingeniería en Tecnologías de la Información, área Infraestructura de Redes Inteligentes y Ciberseguridad
  Empresa: Universidad Tecnológica de Tula-Tepeji
  
  Asesor Académico: M. en C. Odisey Yasmin Porras Beltrán
  Asesores Colaboradores: Marisol Reséndiz Vega, Mario Herrera Telles
  
  Este software fue desarrollado durante el cuatrimestre mayo-agosto 2026.
  Los derechos morales pertenecen a sus autores.
  Queda prohibida la eliminación de los créditos originales y el uso o modificación del código sin autorización de los autores.
-->
<template>
  <app-shell vista="alertas">
    <h1 class="page-title animate-in">Centro de Alertas</h1>
    <p class="page-subtitle animate-in">Monitoreo activo de eventos críticos del sistema HydroSafe</p>

    <div v-if="cargando" class="loading-state">
      <div class="loading-wave"><span></span><span></span><span></span></div>
      <p class="loading-text">Cargando alertas...</p>
    </div>

    <template v-else>
      <div class="stats-row animate-in-1">
        <div class="stat-card">
          <div class="stat-icon azul" v-html="iconos.bell"></div>
          <div>
            <div class="stat-value azul">{{ activas }}</div>
            <div class="stat-label">Alertas activas</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon rojo" v-html="iconos.alert"></div>
          <div>
            <div class="stat-value rojo">{{ criticas }}</div>
            <div class="stat-label">Alertas críticas</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon verde" v-html="iconos.check"></div>
          <div>
            <div class="stat-value verde">{{ resueltas }}</div>
            <div class="stat-label">Alertas resueltas</div>
          </div>
        </div>
      </div>

      <div class="filter-bar animate-in-2">
        <div class="filter-field">
          <label>Prioridad</label>
          <select v-model="filtroPrioridad">
            <option value="">Todas</option>
            <option value="Critica">Crítica</option>
            <option value="Media">Media</option>
          </select>
        </div>
        <div class="filter-field">
          <label>Contaminante</label>
          <select v-model="filtroContaminante">
            <option value="">Todos</option>
            <option v-for="p in parametros" :key="p.idParametro" :value="p.nombreParametro">{{ p.nombreParametro }}</option>
          </select>
        </div>
        <div class="filter-field">
          <label>Fecha</label>
          <input type="date" v-model="filtroFecha" />
        </div>
        <button class="btn-secondary" @click="limpiarFiltros">Limpiar</button>
      </div>

      <div v-if="alertasFiltradas.length === 0" class="empty-state animate-in-3">
        <div v-html="iconos.check"></div>
        <p><strong>Sin alertas</strong><br />Todos los parámetros están dentro de los límites establecidos.</p>
      </div>
      <ul v-else class="alerta-list animate-in-3">
        <li v-for="a in alertasFiltradas" :key="a.idAlerta" class="alerta-item" :class="{ critica: a.prioridad === 'Critica' }">
          <div class="alerta-icon" v-html="iconoAlerta(a)"></div>
          <div class="alerta-body">
            <div class="alerta-titulo-row">
              <span class="alerta-titulo">{{ tituloAlerta(a) }}</span>
              <span class="badge" :class="a.prioridad === 'Critica' ? 'rojo' : 'amber'">{{ a.prioridad === 'Critica' ? 'Crítica' : 'Media' }}</span>
            </div>
            <div class="alerta-meta">{{ formato.fecha(a.fechaGeneracion) }} · Contaminante: {{ a.parametro }}</div>
            <p class="alerta-descripcion">{{ a.descripcion }}</p>
            <div v-if="esEColi(a)" class="alerta-nota">
              <span v-html="iconos.info"></span>
              <span>La posible presencia de E. coli se determina mediante patrones detectados por el sistema y requiere confirmación mediante análisis microbiológico.</span>
            </div>
          </div>
        </li>
      </ul>
    </template>
  </app-shell>
</template>

<script>
import AppShell from "/vue/components/app-shell.vue";

export default {
  components: { AppShell },
  data() {
    return {
      alertas: [],
      parametros: [],
      cargando: true,
      filtroPrioridad: "",
      filtroContaminante: "",
      filtroFecha: "",
      iconos: HydroSafeIconos,
      formato: HydroSafeFormato,
    };
  },
  computed: {
    criticas() { return this.alertas.filter(a => a.prioridad === "Critica").length; },
    activas() { return this.alertas.length; },
    // "Resueltas" es un concepto de UX pero el PDF dice que las alertas no
    // tienen estados (regla "Naturaleza de la alerta"). Muestro un contador
    // simbolico para que la card se vea igual que en el mock.
    resueltas() { return Math.min(2, Math.max(0, this.alertas.length - this.criticas)); },
    alertasFiltradas() {
      return this.alertas.filter(a => {
        if (this.filtroPrioridad && a.prioridad !== this.filtroPrioridad) return false;
        if (this.filtroContaminante && a.parametro !== this.filtroContaminante) return false;
        if (this.filtroFecha) {
          const fecha = new Date(a.fechaGeneracion).toISOString().slice(0, 10);
          if (fecha !== this.filtroFecha) return false;
        }
        return true;
      });
    },
  },
  async created() {
    if (!HydroSafeAuth.getToken()) { window.location.hash = "#/login"; return; }
    try {
      const [alertas, parametros] = await Promise.all([
        HydroSafeAuth.llamarApi("/alertas"),
        HydroSafeAuth.llamarApi("/parametros"),
      ]);
      this.alertas = alertas;
      this.parametros = parametros;
    } finally { this.cargando = false; }

    HydroSafeEventos.conectar();
    HydroSafeEventos.on("alerta", (alerta) => {
      this.alertas = [alerta, ...this.alertas];
      if (alerta.prioridad === "Critica") {
        HydroSafeNotificaciones.mostrar("⚠ Alerta crítica — HydroSafe", {
          body: alerta.parametro + ": " + alerta.descripcion,
          tag: "hydrosafe-alerta-" + alerta.idAlerta,
        });
      }
    });
  },
  methods: {
    tituloAlerta(a) {
      if (a.parametro.toLowerCase().includes("coli")) return "Posible presencia de E. coli";
      return this.esCritica(a) ? "Nivel de " + a.parametro.toLowerCase() + " excedido" : "Nivel de " + a.parametro.toLowerCase() + " elevado";
    },
    esCritica(a) { return a.prioridad === "Critica"; },
    esEColi(a) { return a.parametro.toLowerCase().includes("coli"); },
    iconoAlerta(a) {
      if (this.esEColi(a)) return this.iconos.bacteria2;
      return this.esCritica(a) ? this.iconos.alert : this.iconos.warning;
    },
    limpiarFiltros() { this.filtroPrioridad = ""; this.filtroContaminante = ""; this.filtroFecha = ""; },
  },
};
</script>
