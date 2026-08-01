package mx.edu.uttt.hydrosafe.auth;

import java.time.LocalDateTime;

public class Session {
    public final String token;
    public final String correo;
    public final String nombre;
    public final String rol; // "monitor" o "admin"
    public final LocalDateTime creadoEn;

    public Session(String token, String correo, String nombre, String rol) {
        this.token = token;
        this.correo = correo;
        this.nombre = nombre;
        this.rol = rol;
        this.creadoEn = LocalDateTime.now();
    }
}
