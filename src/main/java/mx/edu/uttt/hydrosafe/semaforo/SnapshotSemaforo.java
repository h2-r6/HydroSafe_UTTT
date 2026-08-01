package mx.edu.uttt.hydrosafe.semaforo;

import java.time.LocalDateTime;

/** estado: "verde" | "amber" | "rojo" */
public record SnapshotSemaforo(int id, String estado, LocalDateTime marcaTemporal) {}
