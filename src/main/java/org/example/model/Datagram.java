package org.example.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Datagram {
    private int busId;
    private double latitud;
    private double longitud;
    private LocalDateTime timestamp;

    // Ajustamos al formato que se ve en tu imagen del CSV: "2018-05-31 00:00:00"
    // Columna 10 parece ser la fecha completa
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Datagram(String csvLine) {
        try {
            // Asumiendo separación por tabs (\t) como se ve en la imagen
            // Si es espacio o coma, ajusta el split
            String[] parts = csvLine.split(",");

            // Indices basados en tu imagen:
            // 0:?, 1:FechaCorta, 2:BusID, 3:?, 4:LatInt, 5:LonInt, ... 10:FechaFull

            this.busId = Integer.parseInt(parts[2]);

            // Dividir por 10^7 para obtener latitud real
            this.latitud = Double.parseDouble(parts[4]) / 10000000.0;
            this.longitud = Double.parseDouble(parts[5]) / 10000000.0;

            // Usamos la columna 10 que tiene hora
            this.timestamp = LocalDateTime.parse(parts[10], DATE_FORMATTER);

        } catch (Exception e) {
            // Si falla el parseo (línea vacía o corrupta), lanzamos excep para que el reader la ignore
            throw new IllegalArgumentException("Linea invalida");
        }
    }

    // Constructor especial para la Poison Pill (Fin de archivo)
    public Datagram(boolean isPoison) {
        this.busId = -1;
    }

    public boolean isPoisonPill() { return busId == -1; }

    public int getBusId() { return busId; }
    public double getLatitud() { return latitud; }
    public double getLongitud() { return longitud; }
    public LocalDateTime getTimestamp() { return timestamp; }

}
