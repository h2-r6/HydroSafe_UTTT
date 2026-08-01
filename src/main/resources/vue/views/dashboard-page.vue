<template>
  <app-shell vista="dashboard">
    <div style="display:flex; align-items:center; gap:12px;" class="animate-in">
      <p class="page-eyebrow" style="margin:0;">Monitoreo · Cisterna Principal</p>
      <span class="live-indicator" :class="{ pulsando: pulsoVivo }" v-html="iconos.droplet"></span>
    </div>
    <h1 class="page-title animate-in">Dashboard</h1>
    <p class="page-subtitle animate-in">{{ actualizadoHace }}</p>

    <div v-if="cargando" class="loading-state">
      <div class="loading-wave"><span></span><span></span><span></span></div>
      <p class="loading-text">Sincronizando lecturas del nodo IoT...</p>
    </div>

    <template v-else>
      <section class="hero-tanque animate-in-1">
        <svg class="hero-tanque-svg" viewBox="0 0 120 170" xmlns="http://www.w3.org/2000/svg">
          <defs>
            <clipPath id="tanqueClip">
              <rect x="17" y="19" width="86" height="130" rx="15" />
            </clipPath>
          </defs>
          <rect x="14" y="16" width="92" height="136" rx="18" class="tanque-contorno" />
          <circle v-for="(cx,i) in [26,50,74,98]" :key="'rt'+i" :cx="cx" cy="21" r="1.3" class="tanque-remache" />
          <circle v-for="(cx,i) in [26,50,74,98]" :key="'rb'+i" :cx="cx" cy="144" r="1.3" class="tanque-remache" />

          <g clip-path="url(#tanqueClip)">
            <rect x="17" y="34" width="86" height="115" :class="['tanque-agua', estadoGeneral.clase]" />
            <g transform="translate(17,29)">
              <path class="tanque-ola" :class="estadoGeneral.clase"
                d="M-24,4 Q-12,0 0,4 T24,4 T48,4 T72,4 T96,4 T120,4 L120,16 L-24,16 Z" />
            </g>
            <g transform="translate(17,32)">
              <path class="tanque-ola-2" :class="estadoGeneral.clase"
                d="M-24,3 Q-12,7 0,3 T24,3 T48,3 T72,3 T96,3 T120,3 L120,14 L-24,14 Z" />
            </g>
            <circle class="tanque-burbuja" cx="40" cy="140" r="1.6" style="animation-delay:0s" />
            <circle class="tanque-burbuja" cx="62" cy="145" r="1.1" style="animation-delay:1.1s" />
            <circle class="tanque-burbuja" cx="80" cy="138" r="1.4" style="animation-delay:2.1s" />
          </g>
        </svg>

        <div class="hero-tanque-body">
          <p class="eyebrow">Estado general del agua</p>
          <h2 class="estado-titulo" :class="estadoGeneral.clase">{{ estadoGeneral.texto }}</h2>
          <p class="estado-descripcion">{{ estadoGeneral.descripcion }}</p>
          <div class="mini-chips">
            <span v-for="p in parametrosConEstado" :key="p.idParametro" class="mini-chip" :class="p.estado.clase">
              {{ p.nombreParametro }}
            </span>
          </div>
        </div>
        <div class="hero-tanque-aside">
          <strong>{{ config.ubicacionNodo || "Cisterna Principal" }}</strong>
          <span>NODO {{ config.nodeId || "CISTERNA-01" }}</span>
          <span class="chip-online">En línea</span>
        </div>
      </section>

      <div class="semana-card animate-in-1" v-if="horasSemana">
        <div class="semana-header">
          <div>
            <div class="semana-title">Estado del semáforo — últimos 7 días</div>
            <div class="semana-sub">Cuánto tiempo pasó el sistema en cada nivel</div>
          </div>
        </div>
        <div class="semana-barra">
          <span class="verde" :style="{ width: pctSemana.verde + '%' }" :title="'Buena: ' + horasSemana.verde.toFixed(1) + 'h'"></span>
          <span class="amber" :style="{ width: pctSemana.amber + '%' }" :title="'Precaución: ' + horasSemana.amber.toFixed(1) + 'h'"></span>
          <span class="rojo" :style="{ width: pctSemana.rojo + '%' }" :title="'Riesgo: ' + horasSemana.rojo.toFixed(1) + 'h'"></span>
        </div>
        <div class="semana-legend">
          <span class="verde">Buena — {{ horasSemana.verde.toFixed(1) }}h</span>
          <span class="amber">Precaución — {{ horasSemana.amber.toFixed(1) }}h</span>
          <span class="rojo">Riesgo — {{ horasSemana.rojo.toFixed(1) }}h</span>
        </div>
      </div>

      <div class="section-eyebrow animate-in-2">Parámetros de calidad</div>
      <div class="param-grid animate-in-2">
        <div v-for="p in parametrosConEstado" :key="p.idParametro" class="tira" :class="{ destello: recienActualizado[p.idParametro] }">
          <div class="tira-swatch" :class="p.estado.clase"></div>
          <div class="tira-body">
            <div class="tira-header">
              <div class="tira-icon" v-html="iconoParametro(p)"></div>
              <span class="badge" :class="p.estado.clase">{{ p.estado.texto }}</span>
            </div>
            <div class="tira-nombre">{{ p.nombreParametro }}</div>
            <template v-if="p.tipo === 'presencia'">
              <div class="tira-valor presencia" :class="{ ok: p.valorMedido === 0 }">
                {{ p.valorMedido > 0 ? "Posible presencia" : "Sin indicios" }}
              </div>
              <div class="tira-limite">Requiere confirmación microbiológica</div>
            </template>
            <template v-else>
              <div class="tira-valor">{{ formatearValor(p.valorMedido) }} <small>{{ p.unidadMedida }}</small></div>
              <div class="tira-limite">LÍMITE {{ p.limiteMax }} {{ p.unidadMedida }}</div>
            </template>
            <div class="tira-tiempo">{{ p.tiempo }}</div>
          </div>
        </div>
      </div>

      <div class="chart-card animate-in-2">
        <div class="live-chart-header">
          <div class="live-chart-title">
            Historial en vivo — últimos 60 min
            <span class="live-badge">En vivo</span>
          </div>
          <select v-model.number="parametroGrafica" @change="cargarGraficaVivo" class="filter-field-inline">
            <option v-for="p in parametros" :key="p.idParametro" :value="p.idParametro">{{ p.nombreParametro }}</option>
          </select>
        </div>
        <svg class="chart-svg" viewBox="0 0 800 260" preserveAspectRatio="none">
          <line v-for="y in [30, 90, 150, 210]" :key="'g'+y" class="grid-line" :x1="40" :y1="y" :x2="780" :y2="y" />
          <polyline class="series" :points="puntosGraficaVivo" style="stroke: var(--cobre); animation-duration: 1s;" />
        </svg>
        <div class="chart-legend"><span style="color: var(--cobre);">{{ nombreParametroGrafica }}</span></div>
      </div>

      <div class="section-eyebrow animate-in-3">Estado del nodo IoT</div>
      <div class="node-grid animate-in-3">
        <div class="node-card">
          <div class="node-icon" v-html="iconos.wifi"></div>
          <div>
            <div class="node-label">Conexión</div>
            <div class="node-value">Conectado</div>
            <div class="node-hint">Wi-Fi · Señal fuerte</div>
          </div>
        </div>
        <div class="node-card">
          <div class="node-icon" v-html="iconos.atom"></div>
          <div>
            <div class="node-label">Wi-Fi</div>
            <div class="node-value">{{ config.wifiSSID || "UTTTLabIoT" }}</div>
            <div class="node-hint">RSSI {{ config.wifiRSSI || -48 }} dBm</div>
          </div>
        </div>
        <div class="node-card">
          <div class="node-icon" v-html="iconos.chip"></div>
          <div>
            <div class="node-label">Nodo ID</div>
            <div class="node-value">{{ config.nodeId || "CISTERNA-01" }}</div>
            <div class="node-hint">{{ config.microcontrolador || "ESP32" }} · Activo</div>
          </div>
        </div>
      </div>
    </template>
  </app-shell>
</template>

<script>
import AppShell from "/vue/components/app-shell.vue";

export default {
  components: { AppShell },
  data() {
    return {
      parametros: [],
      lecturas: [],
      config: {},
      horasSemana: null,
      cargando: true,
      actualizado: new Date(),
      iconos: HydroSafeIconos,
      recienActualizado: {},
      pulsoVivo: false,
      parametroGrafica: 3,
      lecturasGraficaVivo: [],
    };
  },
  computed: {
    actualizadoHace() {
      return "Actualizado " + this.actualizado.toLocaleTimeString("es-MX", { hour: "2-digit", minute: "2-digit" });
    },
    parametrosConEstado() {
      return this.parametros.map(p => {
        const lectura = this.lecturas.find(l => l.idParametro === p.idParametro);
        const valor = lectura ? lectura.valorMedido : null;
        return {
          ...p,
          valorMedido: valor,
          estado: this.calcularEstado(p, valor),
          tiempo: lectura ? HydroSafeFormato.haceCuanto(lectura.marcaTemporal) : "Sin datos",
        };
      });
    },
    estadoGeneral() {
      const estados = this.parametrosConEstado.map(p => p.estado.clase);
      if (estados.includes("rojo")) {
        return { clase: "rojo", texto: "Riesgo", descripcion: "Uno o más parámetros exceden los límites establecidos por la NOM-127-SSA1-2021. Se recomienda atención inmediata." };
      }
      if (estados.includes("amber")) {
        return { clase: "amber", texto: "Precaución", descripcion: "Algunos parámetros se encuentran cercanos al límite permitido. Monitoreo activo." };
      }
      return { clase: "verde", texto: "Buena", descripcion: "Todos los parámetros están dentro de los rangos normativos. Sistema saludable." };
    },
    pctSemana() {
      if (!this.horasSemana) return { verde: 0, amber: 0, rojo: 0 };
      const total = this.horasSemana.verde + this.horasSemana.amber + this.horasSemana.rojo;
      if (total <= 0) return { verde: 100, amber: 0, rojo: 0 };
      return {
        verde: (this.horasSemana.verde / total) * 100,
        amber: (this.horasSemana.amber / total) * 100,
        rojo: (this.horasSemana.rojo / total) * 100,
      };
    },
    nombreParametroGrafica() {
      const p = this.parametros.find(p => p.idParametro === this.parametroGrafica);
      return p ? p.nombreParametro + " (" + p.unidadMedida + ")" : "";
    },
    puntosGraficaVivo() {
      const datos = this.lecturasGraficaVivo;
      if (!datos.length) return "";
      const valores = datos.map(l => l.valorMedido);
      const maxVal = Math.max(...valores, 0.0001);
      const minVal = Math.min(...valores, 0);
      const rango = (maxVal - minVal) || 1;
      const escalaY = v => 30 + (210 - 30) * (1 - (v - minVal) / rango);
      const pasoX = datos.length > 1 ? (780 - 40) / (datos.length - 1) : 0;
      return datos.map((l, i) => (40 + i * pasoX).toFixed(1) + "," + escalaY(l.valorMedido).toFixed(1)).join(" ");
    },
  },
  async created() {
    if (!HydroSafeAuth.getToken()) { window.location.hash = "#/login"; return; }
    try {
      const [parametros, lecturas, config, semana] = await Promise.all([
        HydroSafeAuth.llamarApi("/parametros"),
        HydroSafeAuth.llamarApi("/lecturas/tiempo-real"),
        fetch("/api/configuracion").then(r => r.json()),
        HydroSafeAuth.llamarApi("/semaforo/historial-semana"),
      ]);
      this.parametros = parametros;
      this.lecturas = lecturas;
      this.config = config;
      this.horasSemana = semana.horas;
    } finally { this.cargando = false; }

    await this.cargarGraficaVivo();
    this.conectarEventos();
    HydroSafeNotificaciones.pedirPermiso();
  },
  methods: {
    conectarEventos() {
      HydroSafeEventos.conectar();
      HydroSafeEventos.on("lectura", (lectura) => {
        const idx = this.lecturas.findIndex(l => l.idParametro === lectura.idParametro);
        if (idx >= 0) this.lecturas.splice(idx, 1, lectura); else this.lecturas.push(lectura);
        this.actualizado = new Date();
        this.pulsoVivo = true;
        setTimeout(() => { this.pulsoVivo = false; }, 900);
        this.recienActualizado = { ...this.recienActualizado, [lectura.idParametro]: true };
        setTimeout(() => {
          const copia = { ...this.recienActualizado };
          delete copia[lectura.idParametro];
          this.recienActualizado = copia;
        }, 800);
        if (lectura.idParametro === this.parametroGrafica) this.cargarGraficaVivo();
      });
      HydroSafeEventos.on("alerta", (alerta) => {
        if (alerta.prioridad === "Critica") {
          HydroSafeNotificaciones.mostrar("⚠ Alerta crítica — HydroSafe", {
            body: alerta.parametro + ": " + alerta.descripcion,
            tag: "hydrosafe-alerta-" + alerta.idAlerta,
          });
        }
      });
    },
    async cargarGraficaVivo() {
      this.lecturasGraficaVivo = await HydroSafeAuth.llamarApi("/lecturas/tiempo-real-min?idParametro=" + this.parametroGrafica + "&minutos=60");
    },
    iconoParametro(p) {
      const nombre = p.nombreParametro.toLowerCase();
      if (nombre.includes("flúor") || nombre.includes("fluor")) return HydroSafeIconos.droplet;
      if (nombre.includes("cobre")) return HydroSafeIconos.atom;
      if (nombre.includes("plomo")) return HydroSafeIconos.layers;
      if (nombre.includes("sales") || nombre.includes("tds")) return HydroSafeIconos.layers;
      if (nombre.includes("coli") || p.tipo === "presencia") return HydroSafeIconos.bacteria2;
      return HydroSafeIconos.droplet;
    },
    formatearValor(v) {
      if (v == null) return "-";
      if (v >= 100) return Math.round(v);
      if (v >= 1) return v.toFixed(2);
      return v.toFixed(3);
    },
    calcularEstado(p, valor) {
      if (valor == null) return { clase: "verde", texto: "Sin datos" };
      if (p.tipo === "presencia") {
        return valor > 0 ? { clase: "rojo", texto: "Riesgo" } : { clase: "verde", texto: "Buena" };
      }
      if (valor > p.limiteMax || valor < p.limiteMin) return { clase: "rojo", texto: "Riesgo" };
      const rango = p.limiteMax - p.limiteMin;
      const umbralAdv = p.limiteMax - rango * 0.15;
      if (rango > 0 && valor >= umbralAdv) return { clase: "amber", texto: "Precaución" };
      return { clase: "verde", texto: "Buena" };
    },
  },
};
</script>
