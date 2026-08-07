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
  <app-shell vista="admin/registros">
    <h1 class="page-title animate-in">Registros del sistema</h1>
    <p class="page-subtitle animate-in">Auditoría de acciones administrativas y eventos técnicos del backend.</p>

    <div class="admin-tabs animate-in-1">
      <button class="admin-tab" :class="{ active: tab === 'auditoria' }" @click="tab = 'auditoria'">Auditoría de administración</button>
      <button class="admin-tab" :class="{ active: tab === 'sistema' }" @click="tab = 'sistema'">Eventos del sistema</button>
    </div>

    <div v-if="cargando" class="loading-state">
      <div class="loading-wave"><span></span><span></span><span></span></div>
      <p class="loading-text">Cargando registros...</p>
    </div>

    <template v-else>
      <div v-if="tab === 'auditoria'" class="admin-panel animate-in-2">
        <div v-if="auditoria.length === 0" class="empty-state">
          <p><strong>Sin actividad registrada todavía</strong><br />Las acciones de administración (crear/editar/eliminar) aparecerán aquí.</p>
        </div>
        <table v-else class="data-table">
          <thead><tr><th>Fecha</th><th>Usuario</th><th>Acción</th><th>Detalle</th></tr></thead>
          <tbody>
            <tr v-for="ev in auditoria" :key="ev.id">
              <td>{{ formato.fecha(ev.fecha) }}</td>
              <td><strong>{{ ev.usuario }}</strong></td>
              <td><span class="tag-limite">{{ ev.accion }}</span></td>
              <td>{{ ev.detalle }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-else class="admin-panel animate-in-2">
        <div v-if="eventosSistema.length === 0" class="empty-state">
          <p><strong>Sin eventos registrados todavía</strong></p>
        </div>
        <table v-else class="data-table">
          <thead><tr><th>Fecha</th><th>Tipo</th><th>Mensaje</th></tr></thead>
          <tbody>
            <tr v-for="ev in eventosSistema" :key="ev.id">
              <td>{{ formato.fecha(ev.fecha) }}</td>
              <td><span class="badge" :class="claseTipo(ev.tipo)">{{ ev.tipo }}</span></td>
              <td>{{ ev.mensaje }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>
  </app-shell>
</template>

<script>
import AppShell from "/vue/components/app-shell.vue";

export default {
  components: { AppShell },
  data() {
    return { tab: "auditoria", auditoria: [], eventosSistema: [], cargando: true, formato: HydroSafeFormato };
  },
  async created() {
    if (!HydroSafeAuth.esAdmin()) { window.location.hash = "#/dashboard"; return; }
    try {
      const [auditoria, eventos] = await Promise.all([
        HydroSafeAuth.llamarApi("/admin/auditoria"),
        HydroSafeAuth.llamarApi("/admin/eventos-sistema"),
      ]);
      this.auditoria = auditoria;
      this.eventosSistema = eventos;
    } finally { this.cargando = false; }
  },
  methods: {
    claseTipo(tipo) {
      if (tipo === "error" || tipo === "seguridad") return "rojo";
      if (tipo === "config") return "amber";
      return "verde";
    },
  },
};
</script>
