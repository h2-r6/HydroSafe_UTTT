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
  <app-shell vista="reportes">
    <h1 class="page-title animate-in">Reportes de Calidad del Agua</h1>
    <p class="page-subtitle animate-in">Consulta y descarga reportes históricos del monitoreo realizado por HydroSafe.</p>

    <div class="filter-bar animate-in-1" style="justify-content: space-between;">
      <div style="display:flex; gap:18px; align-items:flex-end; flex-wrap:wrap;">
        <div class="filter-field">
          <label>Fecha inicial</label>
          <input type="date" v-model="fechaInicial" />
        </div>
        <div class="filter-field">
          <label>Fecha final</label>
          <input type="date" v-model="fechaFinal" />
        </div>
        <div class="filter-field">
          <label>Contaminante</label>
          <select v-model="contaminanteFiltro">
            <option value="">Todos</option>
            <option v-for="p in parametros" :key="p.idParametro" :value="p.idParametro">{{ p.nombreParametro }}</option>
          </select>
        </div>
      </div>
      <div style="display:flex; gap:10px;">
        <button class="btn-primary" @click="generar" :disabled="cargando">Generar reporte</button>
        <button class="btn-secondary" @click="limpiar">Limpiar filtros</button>
      </div>
    </div>

    <div v-if="!reporte" class="empty-state animate-in-2">
      <div v-html="iconos.doc"></div>
      <p><strong>Selecciona un rango de fechas</strong><br />y da clic en "Generar reporte" para ver los resultados.</p>
    </div>

    <template v-else>
      <div class="report-toolbar animate-in-2">
        <div class="report-summary">
          <div class="report-summary-item">
            <div class="label">Fecha de generación</div>
            <div class="value">{{ formato.fecha(reporte.reporte.fechaGeneracion) }}</div>
          </div>
          <div class="report-summary-item">
            <div class="label">Periodo consultado</div>
            <div class="value">{{ periodo }}</div>
          </div>
          <div class="report-summary-item">
            <div class="label">Total de registros</div>
            <div class="value">{{ reporte.totalLecturas }} mediciones</div>
          </div>
        </div>
        <div class="report-actions">
          <button class="btn-secondary" @click="descargar('csv')" :disabled="descargando" v-html="iconoConTexto('download', 'CSV')"></button>
          <button class="btn-secondary" @click="descargar('xlsx')" :disabled="descargando" v-html="iconoConTexto('download', 'Excel')"></button>
          <button class="btn-danger" @click="descargar('pdf')" :disabled="descargando" v-html="iconoConTexto('download', 'PDF')"></button>
          <button class="btn-secondary" @click="imprimir" v-html="iconoConTexto('print', 'Imprimir')"></button>
        </div>
      </div>

      <div class="kpi-row animate-in-2">
        <div class="kpi-card">
          <div class="kpi-label">Parámetro más crítico</div>
          <div class="kpi-value rojo" v-if="kpis.masCritico">{{ kpis.masCritico.parametro }} {{ formatearValor(kpis.masCritico.valor) }} {{ kpis.masCritico.unidad }}</div>
          <div class="kpi-value" v-else>—</div>
        </div>
        <div class="kpi-card">
          <div class="kpi-label">Parámetro más estable</div>
          <div class="kpi-value verde" v-if="kpis.masEstable">{{ kpis.masEstable.parametro }} {{ formatearValor(kpis.masEstable.valor) }} {{ kpis.masEstable.unidad }}</div>
          <div class="kpi-value" v-else>—</div>
        </div>
        <div class="kpi-card">
          <div class="kpi-label">Promedio de cumplimiento</div>
          <div class="kpi-value azul" v-if="kpis.promedioRatio !== null">{{ Math.round(kpis.promedioRatio) }}% del límite</div>
          <div class="kpi-value" v-else>—</div>
        </div>
        <div class="kpi-card">
          <div class="kpi-label">Número de registros</div>
          <div class="kpi-value">{{ reporte.totalLecturas }} mediciones</div>
        </div>
      </div>

      <div class="chart-card animate-in-3">
        <div class="chart-title">Historial de mediciones — últimas 24h</div>
        <svg class="chart-svg" viewBox="0 0 800 300" preserveAspectRatio="none">
          <line v-for="(y, i) in [40, 100, 160, 220]" :key="'g'+i" class="grid-line" :x1="40" :y1="y" :x2="780" :y2="y" />
          <polyline v-for="serie in seriesGrafica" :key="serie.id" class="series" :points="serie.puntos" :style="{ stroke: serie.color, animationDelay: (serie.id * 0.15) + 's' }" />
          <text v-for="h in horas" :key="'h'+h.x" :x="h.x" :y="285" class="axis-label" text-anchor="middle">{{ h.label }}</text>
        </svg>
        <div class="chart-legend">
          <span v-for="serie in seriesGrafica" :key="serie.id" :style="{ color: serie.color }">{{ serie.nombre }}</span>
        </div>
      </div>

      <table class="data-table animate-in-3">
        <thead>
          <tr>
            <th>Fecha</th><th>Hora</th>
            <th v-for="p in parametros" :key="'h'+p.idParametro">{{ nombreCortoParam(p) }}</th>
            <th>Estado</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="fila in tablaAgrupada" :key="fila.marcaTemporal">
            <td>{{ formato.fecha(fila.marcaTemporal).split(",")[0] }}</td>
            <td>{{ formato.hora(fila.marcaTemporal) }}</td>
            <td v-for="p in parametros" :key="'c'+p.idParametro" :class="{ 'valor-critico': fila.critico[p.idParametro] }">
              {{ formatearCelda(fila.valores[p.idParametro], p) }}
            </td>
            <td :class="'estado-' + fila.estado.clase">{{ fila.estado.texto }}</td>
          </tr>
        </tbody>
      </table>
    </template>
  </app-shell>
</template>

<script>
import AppShell from "/vue/components/app-shell.vue";

export default {
  components: { AppShell },
  data() {
    return {
      fechaInicial: "",
      fechaFinal: "",
      contaminanteFiltro: "",
      reporte: null,
      parametros: [],
      cargando: false,
      descargando: false,
      iconos: HydroSafeIconos,
      formato: HydroSafeFormato,
    };
  },
  computed: {
    periodo() {
      if (this.fechaInicial && this.fechaFinal) return this.fechaInicial + " al " + this.fechaFinal;
      return "Últimas 24 horas";
    },
    kpis() {
      const lecturas = this.reporte ? this.reporte.lecturas : [];
      // Solo los parametros de tipo "concentracion" tienen un %-del-limite que tenga sentido
      // (a "presencia" como E. coli no le aplica esta cuenta). Nunca se busca por nombre --
      // funciona sin importar que parametros existan, se hayan agregado o se hayan borrado.
      const paramsConcentracion = this.parametros.filter(p => p.tipo === "concentracion" && p.limiteMax > p.limiteMin);
      const filas = [];
      for (const l of lecturas) {
        const p = paramsConcentracion.find(pp => pp.idParametro === l.idParametro);
        if (!p) continue;
        const rango = p.limiteMax - p.limiteMin;
        const ratio = (l.valorMedido - p.limiteMin) / rango;
        filas.push({ parametro: p.nombreParametro, unidad: p.unidadMedida, valor: l.valorMedido, ratio });
      }
      if (!filas.length) return { masCritico: null, masEstable: null, promedioRatio: null };
      const masCritico = filas.reduce((a, b) => (b.ratio > a.ratio ? b : a));
      const masEstable = filas.reduce((a, b) => (b.ratio < a.ratio ? b : a));
      const promedioRatio = (filas.reduce((s, f) => s + f.ratio, 0) / filas.length) * 100;
      return { masCritico, masEstable, promedioRatio };
    },
    tablaAgrupada() {
      if (!this.reporte) return [];
      const agrupadas = new Map();
      this.reporte.lecturas.forEach(l => {
        const key = l.marcaTemporal;
        if (!agrupadas.has(key)) agrupadas.set(key, { marcaTemporal: key, valores: {}, critico: {} });
        agrupadas.get(key).valores[l.idParametro] = l.valorMedido;
      });
      const filas = Array.from(agrupadas.values());
      filas.forEach(f => {
        let peor = "verde";
        this.parametros.forEach(p => {
          const v = f.valores[p.idParametro];
          if (v == null) return;
          if (p.tipo === "presencia" ? v > 0 : (v < p.limiteMin || v > p.limiteMax)) {
            f.critico[p.idParametro] = true;
            peor = "critico";
          } else if (peor !== "critico" && p.tipo !== "presencia") {
            const rango = p.limiteMax - p.limiteMin;
            if (rango > 0 && v >= p.limiteMax - rango * 0.15) peor = "advertencia";
          }
        });
        f.estado = peor === "critico" ? { clase: "critico", texto: "Crítico" }
                : peor === "advertencia" ? { clase: "advertencia", texto: "Advertencia" }
                : { clase: "normal", texto: "Normal" };
      });
      return filas.sort((a, b) => new Date(b.marcaTemporal) - new Date(a.marcaTemporal));
    },
    seriesGrafica() {
      // Solo grafica los primeros 3 parametros de tipo concentracion (Fluor, Cobre, Plomo)
      if (!this.reporte) return [];
      const params = this.parametros.filter(p => p.tipo === "concentracion").slice(0, 3);
      const colores = ["#0ea5e9", "#f59e0b", "#ef4444"];
      return params.map((p, idx) => {
        const puntos = this.reporte.lecturas
          .filter(l => l.idParametro === p.idParametro)
          .sort((a, b) => new Date(a.marcaTemporal) - new Date(b.marcaTemporal));
        if (!puntos.length) return { id: idx, nombre: p.nombreParametro, color: colores[idx], puntos: "" };
        const maxVal = Math.max(...puntos.map(l => l.valorMedido), p.limiteMax * 1.1);
        const escalaY = v => 40 + (220 - 40) * (1 - v / maxVal);
        const pasoX = puntos.length > 1 ? (780 - 40) / (puntos.length - 1) : 0;
        const coords = puntos.map((l, i) => (40 + i * pasoX).toFixed(1) + "," + escalaY(l.valorMedido).toFixed(1)).join(" ");
        return { id: idx, nombre: p.nombreParametro + " (" + p.unidadMedida + ")", color: colores[idx], puntos: coords };
      });
    },
    horas() {
      // Etiquetas del eje X (00:00, 04:00, 08:00, 12:00, 16:00, 20:00)
      return [
        { x: 40, label: "00:00" }, { x: 188, label: "04:00" }, { x: 336, label: "08:00" },
        { x: 484, label: "12:00" }, { x: 632, label: "16:00" }, { x: 780, label: "20:00" },
      ];
    },
  },
  async created() {
    if (!HydroSafeAuth.getToken()) { window.location.hash = "#/login"; return; }
    this.parametros = await HydroSafeAuth.llamarApi("/parametros");
    // Auto-generar con los datos de las ultimas 24h para que la pagina no se vea vacia
    await this.generar();
  },
  methods: {
    async generar() {
      this.cargando = true;
      try {
        const query = new URLSearchParams();
        if (this.fechaInicial) query.set("desde", this.fechaInicial + "T00:00:00");
        if (this.fechaFinal) query.set("hasta", this.fechaFinal + "T23:59:59");
        if (this.contaminanteFiltro) query.set("idParametro", this.contaminanteFiltro);
        this.reporte = await HydroSafeAuth.llamarApi("/reportes?" + query.toString());
      } finally { this.cargando = false; }
    },
    limpiar() { this.fechaInicial = ""; this.fechaFinal = ""; this.contaminanteFiltro = ""; this.reporte = null; },
    imprimir() { window.print(); },
    async descargar(formato) {
      this.descargando = true;
      try {
        const query = new URLSearchParams();
        query.set("formato", formato);
        if (this.fechaInicial) query.set("desde", this.fechaInicial + "T00:00:00");
        if (this.fechaFinal) query.set("hasta", this.fechaFinal + "T23:59:59");
        if (this.contaminanteFiltro) query.set("idParametro", this.contaminanteFiltro);

        const res = await fetch("/api/reportes/exportar?" + query.toString(), {
          headers: { Authorization: "Bearer " + HydroSafeAuth.getToken() },
        });
        if (!res.ok) throw new Error("No se pudo generar el archivo");
        const blob = await res.blob();
        const url = URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = "reporte_hydrosafe." + formato;
        document.body.appendChild(a);
        a.click();
        a.remove();
        URL.revokeObjectURL(url);
      } catch (e) {
        alert("No se pudo descargar el reporte: " + e.message);
      } finally {
        this.descargando = false;
      }
    },
    nombreCortoParam(p) {
      const n = p.nombreParametro.toLowerCase();
      if (n.includes("coli")) return "E. coli";
      if (n.includes("sales")) return "Sales (mg/L)";
      return p.nombreParametro + " (" + p.unidadMedida + ")";
    },
    formatearValor(v) {
      if (v == null) return "-";
      if (v >= 100) return Math.round(v);
      if (v >= 1) return v.toFixed(2);
      return v.toFixed(3);
    },
    formatearCelda(v, p) {
      if (v == null) return "-";
      if (p.tipo === "presencia") return v > 0 ? "Posible presencia" : "Sin indicios";
      return this.formatearValor(v);
    },
    iconoConTexto(icono, texto) { return HydroSafeIconos[icono] + '<span>' + texto + '</span>'; },
  },
};
</script>
