<template>
  <div class="login-page">
    <div class="login-blueprint"></div>
    <svg class="login-schema" width="520" height="520" viewBox="0 0 520 520" style="right:-120px; top:-60px;">
      <circle cx="260" cy="260" r="150" fill="none" stroke="#2fa88f" stroke-width="1" />
      <circle cx="260" cy="260" r="170" fill="none" stroke="#2fa88f" stroke-width="1" stroke-dasharray="2 6" />
      <line x1="260" y1="20" x2="260" y2="500" stroke="#2fa88f" stroke-width="0.5" />
      <line x1="20" y1="260" x2="500" y2="260" stroke="#2fa88f" stroke-width="0.5" />
      <line x1="410" y1="260" x2="500" y2="260" stroke="#2fa88f" stroke-width="1" />
      <circle cx="450" cy="260" r="9" fill="none" stroke="#2fa88f" stroke-width="1" />
      <line x1="260" y1="110" x2="260" y2="20" stroke="#2fa88f" stroke-width="1" />
      <circle cx="260" cy="65" r="9" fill="none" stroke="#2fa88f" stroke-width="1" />
    </svg>

    <div class="login-layout">
      <div class="login-card">
        <div class="login-brand">
          <div class="login-drop">
            <svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 2s-6 8-6 13a6 6 0 0 0 12 0c0-5-6-13-6-13z"/></svg>
          </div>
          <h1>HydroSafe</h1>
          <p>MONITOREO DE CALIDAD DEL AGUA · UTTT</p>
        </div>

        <form class="login-form" @submit.prevent="iniciarSesion">
          <label>Correo institucional</label>
          <input v-model="correo" type="email" placeholder="usuario@uttt.edu.mx" autocomplete="username" required />

          <label>Contraseña</label>
          <input v-model="contrasena" type="password" placeholder="••••••••" autocomplete="current-password" required />

          <label class="check-row">
            <input type="checkbox" v-model="recordarme" /> Recordarme en este equipo
          </label>

          <p v-if="error" class="login-error">{{ error }}</p>

          <button type="submit" class="btn-primary wide" :disabled="cargando">
            <span v-if="cargando">Verificando...</span>
            <span v-else>Iniciar sesión</span>
          </button>

          <p style="margin-top:20px; font-family: var(--f-mono); font-size:11px; color:#5c7871; text-align:center; line-height:1.8;">
            MONITOR &nbsp;ing.mendoza@uttt.edu.mx&nbsp; / &nbsp;hydrosafe2026<br />
            ADMIN &nbsp;admin@uttt.edu.mx&nbsp; / &nbsp;admin2026
          </p>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      correo: "", contrasena: "", recordarme: false, cargando: false, error: "",
    };
  },
  methods: {
    async iniciarSesion() {
      this.error = "";
      this.cargando = true;
      try {
        const res = await fetch("/api/usuarios/login", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ correo: this.correo, contrasena: this.contrasena }),
        });
        if (!res.ok) throw new Error("Credenciales invalidas");
        const data = await res.json();
        HydroSafeAuth.guardarSesion(data.token, data.correo, data.nombre, data.rol);
        window.location.hash = data.rol === "admin" ? "#/admin/parametros" : "#/dashboard";
      } catch (e) {
        this.error = "Correo o contraseña incorrectos. Intenta de nuevo.";
      } finally {
        this.cargando = false;
      }
    },
  },
};
</script>