package org.example.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Datagram {
    private int busId;
    private int stopId;
    private int lineId;
    private int tripId;
    private double latitud;
    private double longitud;
    private int odometer;
    private LocalDateTime timestamp;

    private final boolean isPoison;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Datagram(String csvLine) {
        this.isPoison = false;

        try {
            String[] parts = csvLine.split(",");

            if (parts.length < 12) {
                throw new IllegalArgumentException("Línea incompleta: esperadas 12 columnas, encontradas " + parts.length);
            }

            this.busId = Integer.parseInt(parts[11].trim());

            this.stopId = Integer.parseInt(parts[2].trim());
            this.lineId = Integer.parseInt(parts[7].trim());
            this.tripId = Integer.parseInt(parts[8].trim());
            this.odometer = Integer.parseInt(parts[3].trim());

            this.latitud = Double.parseDouble(parts[4].trim()) / 10000000.0;
            this.longitud = Double.parseDouble(parts[5].trim()) / 10000000.0;

            this.timestamp = LocalDateTime.parse(parts[10].trim(), DATE_FORMATTER);

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Error parseando números: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new IllegalArgumentException("Línea inválida: " + e.getMessage(), e);
        }
    }


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


    public boolean isPoisonPill() {
        return isPoison;
    }


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


    public boolean hasValidCoordinates() {
        return latitud >= -90 && latitud <= 90 &&
                longitud >= -180 && longitud <= 180 &&
                latitud != 0 && longitud != 0;
    }
}