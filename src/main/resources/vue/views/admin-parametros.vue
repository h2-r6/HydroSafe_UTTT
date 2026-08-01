<template>
  <app-shell vista="admin/parametros">
    <h1 class="page-title animate-in">Parámetros normativos</h1>
    <p class="page-subtitle animate-in">Ajusta los límites de la NOM-127-SSA1-2021 o agrega parámetros nuevos para futuros sensores IoT.</p>

    <div class="admin-panel animate-in-1">
      <div class="admin-panel-header">
        <div>
          <div class="admin-panel-title">Catálogo de parámetros</div>
          <div class="admin-panel-desc">Estos son los rangos que el motor de alertas usa para evaluar cada lectura del nodo IoT.</div>
        </div>
        <button class="btn-primary" @click="abrirNuevo" v-html="iconoConTexto('plus', 'Agregar parámetro')"></button>
      </div>

      <table class="data-table">
        <thead>
          <tr><th>ID</th><th>Nombre</th><th>Unidad</th><th>Tipo</th><th>Límite mínimo</th><th>Límite máximo</th><th></th></tr>
        </thead>
        <tbody>
          <tr v-for="p in parametros" :key="p.idParametro">
            <td>#{{ p.idParametro }}</td>
            <td><strong>{{ p.nombreParametro }}</strong></td>
            <td>{{ p.unidadMedida }}</td>
            <td>{{ etiquetaTipo(p.tipo) }}</td>
            <td><span class="tag-limite">{{ p.limiteMin }}</span></td>
            <td><span class="tag-limite">{{ p.limiteMax }}</span></td>
            <td style="text-align:right; white-space:nowrap;">
              <button class="icon-btn" @click="abrirEditar(p)" title="Editar" v-html="iconos.edit"></button>
              <button class="icon-btn danger" @click="eliminar(p)" title="Eliminar" v-html="iconos.trash"></button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="editando" class="admin-panel animate-in-2" style="margin-top: 18px;">
      <div class="admin-panel-header">
        <div>
          <div class="admin-panel-title">{{ editando.idParametro ? "Editar parámetro" : "Nuevo parámetro" }}</div>
          <div class="admin-panel-desc">Los cambios afectan de inmediato al motor de alertas y al dashboard.</div>
        </div>
        <button class="btn-ghost" @click="editando = null">Cancelar</button>
      </div>
      <div class="form-grid">
        <div class="form-field">
          <label>Nombre</label>
          <input v-model="editando.nombreParametro" placeholder="ej. Nitratos" />
        </div>
        <div class="form-field">
          <label>Unidad de medida</label>
          <input v-model="editando.unidadMedida" placeholder="mg/L" />
        </div>
        <div class="form-field">
          <label>Tipo de parámetro</label>
          <select v-model="editando.tipo">
            <option value="concentracion">Concentración (numérico)</option>
            <option value="presencia">Presencia (0/1)</option>
            <option value="rango">Rango permitido (min/max)</option>
          </select>
        </div>
        <div class="form-field">
          <label>Límite mínimo</label>
          <input type="number" step="0.001" v-model.number="editando.limiteMin" />
        </div>
        <div class="form-field">
          <label>Límite máximo</label>
          <input type="number" step="0.001" v-model.number="editando.limiteMax" />
        </div>
      </div>
      <div style="display:flex; gap:10px; justify-content:flex-end;">
        <button class="btn-secondary" @click="editando = null">Cancelar</button>
        <button class="btn-primary" @click="guardar" :disabled="guardando">Guardar cambios</button>
      </div>
    </div>
  </app-shell>
</template>

<script>
import AppShell from "/vue/components/app-shell.vue";

export default {
  components: { AppShell },
  data() {
    return { parametros: [], editando: null, guardando: false, iconos: HydroSafeIconos };
  },
  async created() {
    if (!HydroSafeAuth.esAdmin()) { window.location.hash = "#/dashboard"; return; }
    this.parametros = await HydroSafeAuth.llamarApi("/parametros");
  },
  methods: {
    etiquetaTipo(t) {
      return { concentracion: "Concentración", presencia: "Presencia", rango: "Rango" }[t] || t;
    },
    abrirEditar(p) { this.editando = { ...p }; },
    abrirNuevo() { this.editando = { idParametro: null, nombreParametro: "", unidadMedida: "mg/L", tipo: "concentracion", limiteMin: 0, limiteMax: 1 }; },
    async guardar() {
      this.guardando = true;
      try {
        const url = this.editando.idParametro ? "/admin/parametros/editar" : "/admin/parametros";
        await HydroSafeAuth.llamarApi(url, { method: "POST", body: JSON.stringify(this.editando) });
        this.parametros = await HydroSafeAuth.llamarApi("/parametros");
        this.editando = null;
      } finally { this.guardando = false; }
    },
    async eliminar(p) {
      if (!confirm("¿Eliminar el parámetro \"" + p.nombreParametro + "\"? Las lecturas históricas se conservan.")) return;
      await HydroSafeAuth.llamarApi("/admin/parametros/eliminar?id=" + p.idParametro, { method: "POST", body: "{}" });
      this.parametros = await HydroSafeAuth.llamarApi("/parametros");
    },
    iconoConTexto(icono, texto) { return HydroSafeIconos[icono] + '<span>' + texto + '</span>'; },
  },
};
</script>
