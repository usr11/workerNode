package org.example.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Datagram {
    // Campos principales
    private int busId;           // Columna 11
    private int stopId;          // Columna 2
    private int lineId;          // Columna 7
    private int tripId;          // Columna 8
    private double latitud;      // Columna 4
    private double longitud;     // Columna 5
    private int odometer;        // Columna 3
    private LocalDateTime timestamp;  // Columna 10

    // Flag para POISON_PILL
    private final boolean isPoison;

    // Formato del timestamp en el CSV
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Constructor para datagramas reales del CSV
     *
     * Formato CSV:
     * 0:eventType, 1:registerDate, 2:stopId, 3:odometer,
     * 4:latitude, 5:longitude, 6:taskId, 7:lineId,
     * 8:tripId, 9:unknown1, 10:datagramDate, 11:busId
     */
    public Datagram(String csvLine) {
        this.isPoison = false;

        try {
            String[] parts = csvLine.split(",");

            // Validar que tengamos suficientes columnas
            if (parts.length < 12) {
                throw new IllegalArgumentException("Línea incompleta: esperadas 12 columnas, encontradas " + parts.length);
            }

            // ✅ CORRECCIÓN CRÍTICA: busId está en columna 11
            this.busId = Integer.parseInt(parts[11].trim());

            // Campos adicionales útiles
            this.stopId = Integer.parseInt(parts[2].trim());
            this.lineId = Integer.parseInt(parts[7].trim());
            this.tripId = Integer.parseInt(parts[8].trim());
            this.odometer = Integer.parseInt(parts[3].trim());

            // Coordenadas (dividir por 10^7)
            this.latitud = Double.parseDouble(parts[4].trim()) / 10000000.0;
            this.longitud = Double.parseDouble(parts[5].trim()) / 10000000.0;

            // Timestamp
            this.timestamp = LocalDateTime.parse(parts[10].trim(), DATE_FORMATTER);

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Error parseando números: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new IllegalArgumentException("Línea inválida: " + e.getMessage(), e);
        }
    }

    /**
     * Constructor especial SOLO para POISON_PILL
     */
    public Datagram(boolean isPoison) {
        this.isPoison = isPoison;
        this.busId = Integer.MIN_VALUE;
        this.stopId = 0;
        this.lineId = 0;
        this.tripId = 0;
        this.odometer = 0;
        this.latitud = 0;
        this.longitud = 0;
        this.timestamp = null;
    }

    /**
     * Verifica si es una POISON_PILL usando el flag explícito
     */
    public boolean isPoisonPill() {
        return isPoison;
    }

    // Getters
    public int getBusId() { return busId; }
    public int getStopId() { return stopId; }
    public int getLineId() { return lineId; }
    public int getTripId() { return tripId; }
    public int getOdometer() { return odometer; }
    public double getLatitud() { return latitud; }
    public double getLongitud() { return longitud; }
    public LocalDateTime getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        if (isPoison) return "POISON_PILL";
        return String.format(
                "Datagram{busId=%d, stopId=%d, lineId=%d, tripId=%d, lat=%.6f, lon=%.6f, time=%s}",
                busId, stopId, lineId, tripId, latitud, longitud, timestamp
        );
    }

    /**
     * Validación de datos
     */
    public boolean hasValidCoordinates() {
        return latitud >= -90 && latitud <= 90 &&
                longitud >= -180 && longitud <= 180 &&
                latitud != 0 && longitud != 0;
    }
}