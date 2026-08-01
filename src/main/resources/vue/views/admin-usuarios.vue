<template>
  <app-shell vista="admin/usuarios">
    <h1 class="page-title animate-in">Usuarios del sistema</h1>
    <p class="page-subtitle animate-in">Da de alta encargados de monitoreo y administradores. Cada rol tiene sus propios permisos.</p>

    <div class="admin-panel animate-in-1">
      <div class="admin-panel-header">
        <div>
          <div class="admin-panel-title">Cuentas activas</div>
          <div class="admin-panel-desc">Los "monitor" solo ven el dashboard y las alertas. Los "admin" además pueden editar todo.</div>
        </div>
        <button class="btn-primary" @click="abrirNuevo" v-html="iconoConTexto('plus', 'Nuevo usuario')"></button>
      </div>

      <table class="data-table">
        <thead>
          <tr><th>ID</th><th>Nombre</th><th>Correo</th><th>Rol</th><th></th></tr>
        </thead>
        <tbody>
          <tr v-for="u in usuarios" :key="u.idUsuario">
            <td>#{{ u.idUsuario }}</td>
            <td><strong>{{ u.nombre }}</strong></td>
            <td>{{ u.correo }}</td>
            <td><span class="admin-badge-rol" :class="u.rol">{{ u.rol === 'admin' ? 'Administrador' : 'Monitor' }}</span></td>
            <td style="text-align:right;">
              <button class="icon-btn danger" @click="eliminar(u)" title="Eliminar" v-html="iconos.trash"></button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="nuevo" class="admin-panel animate-in-2" style="margin-top: 18px;">
      <div class="admin-panel-header">
        <div>
          <div class="admin-panel-title">Nuevo usuario</div>
          <div class="admin-panel-desc">Los datos quedan en memoria por ahora. Cuando conectes Firebird se persiste en la tabla ENCARGADO_MONITOREO.</div>
        </div>
        <button class="btn-ghost" @click="nuevo = null">Cancelar</button>
      </div>
      <div class="form-grid">
        <div class="form-field">
          <label>Nombre completo</label>
          <input v-model="nuevo.nombre" placeholder="Ing. Jane Doe" />
        </div>
        <div class="form-field">
          <label>Correo institucional</label>
          <input v-model="nuevo.correo" type="email" placeholder="usuario@uttt.edu.mx" />
        </div>
        <div class="form-field">
          <label>Contraseña</label>
          <input v-model="nuevo.contrasena" type="password" placeholder="mínimo 8 caracteres" />
        </div>
        <div class="form-field">
          <label>Rol</label>
          <select v-model="nuevo.rol">
            <option value="monitor">Encargado de Monitoreo</option>
            <option value="admin">Administrador</option>
          </select>
        </div>
      </div>
      <div style="display:flex; gap:10px; justify-content:flex-end;">
        <button class="btn-secondary" @click="nuevo = null">Cancelar</button>
        <button class="btn-primary" @click="crear" :disabled="creando">Crear usuario</button>
      </div>
    </div>
  </app-shell>
</template>

<script>
import AppShell from "/vue/components/app-shell.vue";

export default {
  components: { AppShell },
  data() {
    return { usuarios: [], nuevo: null, creando: false, iconos: HydroSafeIconos };
  },
  async created() {
    if (!HydroSafeAuth.esAdmin()) { window.location.hash = "#/dashboard"; return; }
    await this.recargar();
  },
  methods: {
    async recargar() { this.usuarios = await HydroSafeAuth.llamarApi("/admin/usuarios"); },
    abrirNuevo() { this.nuevo = { nombre: "", correo: "", contrasena: "", rol: "monitor" }; },
    async crear() {
      this.creando = true;
      try {
        await HydroSafeAuth.llamarApi("/admin/usuarios", { method: "POST", body: JSON.stringify(this.nuevo) });
        await this.recargar();
        this.nuevo = null;
      } finally { this.creando = false; }
    },
    async eliminar(u) {
      if (!confirm("¿Eliminar al usuario \"" + u.nombre + "\"?")) return;
      await HydroSafeAuth.llamarApi("/admin/usuarios/eliminar?id=" + u.idUsuario, { method: "POST", body: "{}" });
      await this.recargar();
    },
    iconoConTexto(icono, texto) { return HydroSafeIconos[icono] + '<span>' + texto + '</span>'; },
  },
};
</script>
