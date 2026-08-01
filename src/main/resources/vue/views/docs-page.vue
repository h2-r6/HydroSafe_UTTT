<template>
  <app-shell vista="docs">
    <h1 class="page-title animate-in">Documentación</h1>
    <p class="page-subtitle animate-in">Manual de uso, presentación del proyecto y enlaces de referencia.</p>

    <div class="docs-grid animate-in-1">
      <div>
        <div class="docs-section">
          <h3>¿Qué es HydroSafe?</h3>
          <p>HydroSafe es un sistema de monitoreo inteligente de calidad del agua desarrollado para
          UTTT. Un nodo IoT (ESP32) mide continuamente Flúor, Cobre, Plomo, Sales (TDS) y posible
          presencia de E. coli, y esta plataforma los evalúa contra la NOM-127-SSA1-2021, generando
          alertas automáticas cuando algún parámetro se sale de rango.</p>
        </div>

        <div class="docs-section">
          <h3>Guía rápida — Encargado de Monitoreo</h3>
          <ol>
            <li>Inicia sesión con tu correo institucional.</li>
            <li>En <strong>Dashboard</strong> puedes ver el estado general (semáforo), cada parámetro con su valor actual y una gráfica en vivo.</li>
            <li>En <strong>Alertas</strong> revisa el historial completo de eventos críticos y de advertencia.</li>
            <li>En <strong>Reportes</strong> puedes filtrar por fecha/parámetro y descargar en CSV, Excel o PDF.</li>
          </ol>
        </div>

        <div class="docs-section" v-if="esAdmin">
          <h3>Guía rápida — Administrador</h3>
          <ol>
            <li><strong>Parámetros:</strong> ajusta límites normativos o agrega parámetros nuevos para sensores futuros.</li>
            <li><strong>Usuarios:</strong> da de alta más monitores o administradores.</li>
            <li><strong>Configuración:</strong> edita el nombre del nodo, WiFi y los enlaces de esta página.</li>
            <li><strong>Registros:</strong> consulta la auditoría de acciones administrativas y el log técnico del sistema.</li>
          </ol>
        </div>

        <div class="docs-section">
          <h3>Preguntas frecuentes</h3>
          <p><strong>¿Por qué "Posible E. coli" no da un número exacto?</strong><br />
          El sistema detecta patrones que sugieren posible presencia, pero solo un análisis
          microbiológico de laboratorio puede confirmarlo — por eso siempre se muestra con esa nota.</p>
          <p><strong>¿Qué significa el nivel "Precaución" (ámbar)?</strong><br />
          El parámetro sigue dentro del límite normativo, pero ya está a menos del 15% del techo
          permitido, así que vale la pena vigilarlo de cerca.</p>
        </div>
      </div>

      <div>
        <div class="docs-section">
          <h3>Presentación del proyecto</h3>
          <div v-if="infoDoc.disponible" class="upload-box">
            <div v-html="iconos.doc"></div>
            <p style="margin:0; font-size:13px;">{{ infoDoc.nombreOriginal }}</p>
            <button class="btn-secondary" style="margin-top:10px;" @click="descargarPresentacion" v-html="iconoConTexto('download', 'Descargar')"></button>
            <div class="archivo-actual" v-html="iconoConTexto('check', 'Subida ' + formato.fecha(infoDoc.subidoEn))"></div>
          </div>
          <div v-else class="upload-box">
            <div v-html="iconos.doc"></div>
            <p style="margin:0; font-size:13px;">Todavía no se ha subido la presentación.</p>
          </div>
          <template v-if="esAdmin">
            <label class="btn-primary wide" style="margin-top:12px; text-align:center; cursor:pointer;">
              {{ subiendo ? "Subiendo..." : "Subir presentación (PPTX / PDF)" }}
              <input type="file" accept=".pptx,.ppt,.pdf" @change="subirPresentacion" :disabled="subiendo" />
            </label>
          </template>
        </div>

        <div class="docs-section">
          <h3>Video demo</h3>
          <div v-if="urlEmbedVideo" class="video-embed">
            <iframe :src="urlEmbedVideo" allowfullscreen></iframe>
          </div>
          <div v-else class="video-empty">
            <div v-html="iconos.info"></div>
            <span>Aún no hay video enlazado.<br />{{ esAdmin ? "Pégalo en Configuración cuando lo tengas." : "" }}</span>
          </div>
        </div>

        <div class="docs-section">
          <h3>Repositorio</h3>
          <a v-if="config.githubUrl" :href="config.githubUrl" target="_blank" rel="noopener" class="link-card">
            <span class="icon" v-html="iconos.chip"></span>
            <div><strong>Ver código en GitHub</strong><span>{{ config.githubUrl }}</span></div>
          </a>
          <p v-else style="color:var(--hs-muted); font-size:13px;">Aún no hay repositorio enlazado.</p>
        </div>
      </div>
    </div>
  </app-shell>
</template>

<script>
import AppShell from "/vue/components/app-shell.vue";

export default {
  components: { AppShell },
  data() {
    return {
      esAdmin: HydroSafeAuth.esAdmin(),
      config: {},
      infoDoc: { disponible: false },
      subiendo: false,
      iconos: HydroSafeIconos,
      formato: HydroSafeFormato,
    };
  },
  computed: {
    urlEmbedVideo() {
      const url = this.config.videoUrl;
      if (!url) return null;
      const watch = url.match(/[?&]v=([^&]+)/);
      if (watch) return "https://www.youtube.com/embed/" + watch[1];
      const corto = url.match(/youtu\.be\/([^?&]+)/);
      if (corto) return "https://www.youtube.com/embed/" + corto[1];
      const yaEmbed = url.match(/youtube\.com\/embed\//);
      if (yaEmbed) return url;
      return null; // otros hosts: se ignora el embed para no romper por X-Frame-Options
    },
  },
  async created() {
    if (!HydroSafeAuth.getToken()) { window.location.hash = "#/login"; return; }
    const [config, infoDoc] = await Promise.all([
      fetch("/api/configuracion").then(r => r.json()),
      HydroSafeAuth.llamarApi("/documentos/presentacion"),
    ]);
    this.config = config;
    this.infoDoc = infoDoc;
  },
  methods: {
    iconoConTexto(icono, texto) { return HydroSafeIconos[icono] + '<span>' + texto + '</span>'; },
    async subirPresentacion(ev) {
      const archivo = ev.target.files[0];
      if (!archivo) return;
      this.subiendo = true;
      try {
        const bytes = await archivo.arrayBuffer();
        const res = await fetch("/api/admin/documentos/presentacion?nombre=" + encodeURIComponent(archivo.name), {
          method: "POST",
          headers: { Authorization: "Bearer " + HydroSafeAuth.getToken() },
          body: bytes,
        });
        if (!res.ok) throw new Error("Error al subir");
        this.infoDoc = await res.json();
      } catch (e) {
        alert("No se pudo subir el archivo: " + e.message);
      } finally {
        this.subiendo = false;
        ev.target.value = "";
      }
    },
    async descargarPresentacion() {
      const res = await fetch("/api/documentos/presentacion/descargar", {
        headers: { Authorization: "Bearer " + HydroSafeAuth.getToken() },
      });
      const blob = await res.blob();
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = this.infoDoc.nombreOriginal || "presentacion";
      document.body.appendChild(a);
      a.click();
      a.remove();
      URL.revokeObjectURL(url);
    },
  },
};
</script>
