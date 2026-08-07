/**
 * HydroSafe (UTTT) — Sistema de Monitoreo de la Calidad del Agua
 * 
 * Autores:
 *   - Maria Fernanda Aldana Jiménez
 *   - Natali Isabel Chávez Alpízar
 *   - Hiyadir Raúlciel Barrera Cuervo
 * 
 * Universidad Tecnológica de Tula-Tepeji
 * Programa Educativo: Ingeniería en Tecnologías de la Información, área Infraestructura de Redes Inteligentes y Ciberseguridad
 * Empresa: Universidad Tecnológica de Tula-Tepeji
 * 
 * Asesor Académico: M. en C. Odisey Yasmin Porras Beltrán
 * Asesores Colaboradores: Marisol Reséndiz Vega, Mario Herrera Telles
 * 
 * Este software fue desarrollado durante el cuatrimestre mayo-agosto 2026.
 * Los derechos morales pertenecen a sus autores.
 * Queda prohibida la eliminación de los créditos originales y el uso o modificación del código sin autorización de los autores.
 */
package mx.edu.uttt.hydrosafe.seguridad;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Hash de contrasenas con PBKDF2-HMAC-SHA256 (viene en el JDK via javax.crypto,
 * sin librerias externas). No es BCrypt exactamente, pero es la misma familia
 * de algoritmo -- lento a proposito, con sal por usuario -- y esta recomendado
 * por NIST SP 800-63B como alternativa valida quand no se puede usar bcrypt/argon2.
 *
 * Formato guardado: "iteraciones:sal_base64:hash_base64"
 */
public class PasswordHasher {

    private static final int ITERACIONES = 120_000;
    private static final int LONGITUD_CLAVE_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String hash(String contrasenaPlano) {
        byte[] sal = new byte[16];
        RANDOM.nextBytes(sal);
        byte[] derivado = pbkdf2(contrasenaPlano.toCharArray(), sal, ITERACIONES);
        return ITERACIONES + ":" + Base64.getEncoder().encodeToString(sal) + ":" + Base64.getEncoder().encodeToString(derivado);
    }

    public static boolean verificar(String contrasenaPlano, String hashGuardado) {
        try {
            String[] partes = hashGuardado.split(":");
            int iteraciones = Integer.parseInt(partes[0]);
            byte[] sal = Base64.getDecoder().decode(partes[1]);
            byte[] esperado = Base64.getDecoder().decode(partes[2]);
            byte[] calculado = pbkdf2(contrasenaPlano.toCharArray(), sal, iteraciones);
            return tiempoConstante(esperado, calculado);
        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] pbkdf2(char[] contrasena, byte[] sal, int iteraciones) {
        try {
            PBEKeySpec spec = new PBEKeySpec(contrasena, sal, iteraciones, LONGITUD_CLAVE_BITS);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return skf.generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** Compara byte a byte sin salir antes de tiempo, para no filtrar info por timing attack. */
    private static boolean tiempoConstante(byte[] a, byte[] b) {
        if (a.length != b.length) return false;
        int resultado = 0;
        for (int i = 0; i < a.length; i++) resultado |= a[i] ^ b[i];
        return resultado == 0;
    }
}
