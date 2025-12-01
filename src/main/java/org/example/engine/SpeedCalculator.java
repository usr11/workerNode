package org.example.engine;

import org.example.cache.JGraphT_Topology;
import org.example.graph.Arch;
import org.example.logic.GeoUtils;
import org.example.model.Datagram;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class SpeedCalculator implements Runnable {

    private final BlockingQueue<Datagram> queue;
    private final JGraphT_Topology topology;

    public final AtomicInteger processed = new AtomicInteger(0);
    public final AtomicInteger matched = new AtomicInteger(0);
    public final AtomicInteger invalidCoords = new AtomicInteger(0);
    public final AtomicInteger validButNoMatch = new AtomicInteger(0);
    public final AtomicInteger speedCalculated = new AtomicInteger(0);

    // Calculo de velocidades por arco
    private final Map<String, SpeedStats> archSpeedMap = new HashMap<>();

    private final Map<Integer, Datagram> lastPositionByBus = new HashMap<>();

    public SpeedCalculator(BlockingQueue<Datagram> queue) {
        this.queue = queue;
        this.topology = JGraphT_Topology.getInstance();
    }

    @Override
    public void run() {
        System.out.println("[Calculator] ═══ INICIANDO PROCESAMIENTO ═══");
        System.out.println("[Calculator] Esperando datagramas de la cola...");

        long lastReport = System.currentTimeMillis();
        int lastProcessed = 0;

        try {
            while (true) {
                Datagram d = queue.take();

                if (d.isPoisonPill()) {
                    System.out.println("[Calculator] POISON_PILL recibida. Finalizando...");
                    break;
                }

                try {
                    // validar coordenadas
                    if (!d.hasValidCoordinates()) {
                        invalidCoords.incrementAndGet();
                        processed.incrementAndGet();
                        continue;
                    }

                    Arch arco = topology.findNearestArch(d.getLatitud(), d.getLongitud());

                    if (arco != null) {
                        matched.incrementAndGet();

                        // calcular velocidad
                        calculateSpeed(d, arco);

                    } else {
                        validButNoMatch.incrementAndGet();
                    }

                    int total = processed.incrementAndGet();

                    long now = System.currentTimeMillis();
                    if (now - lastReport >= 1000) {
                        int rate = total - lastProcessed;
                        System.out.println(String.format(
                                "[Calculator] Total: %,7d | Match: %,7d (%.1f%%) | Speed: %,6d | Arcos: %,4d | Rate: %,5d/s | Q: %d",
                                total,
                                matched.get(),
                                (matched.get() * 100.0 / total),
                                speedCalculated.get(),
                                archSpeedMap.size(),
                                rate,
                                queue.size()
                        ));
                        lastReport = now;
                        lastProcessed = total;
                    }

                } catch (Exception e) {
                    System.err.println("[Calculator] Error procesando: " + e.getMessage());
                    if (processed.get() < 10) {
                        e.printStackTrace();
                    }
                    processed.incrementAndGet();
                }
            }

            printFinalReport();

        } catch (InterruptedException e) {
            System.err.println("[Calculator] Hilo interrumpido.");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println("[Calculator] ERROR CRÍTICO: " + e.getMessage());
            e.printStackTrace();
        }
    }


    // Calcula la velocidad del bus en el arco usando la última posición conocida
    private void calculateSpeed(Datagram current, Arch arco) {
        int busId = current.getBusId();

        Datagram prev = lastPositionByBus.get(busId);

        if (prev != null) {
            // Calcular delta de tiempo (en segundos)
            Duration delta = Duration.between(prev.getTimestamp(), current.getTimestamp());
            long seconds = delta.getSeconds();

            if (seconds > 0 && seconds < 300) {

                double distance = GeoUtils.haversine(
                        prev.getLatitud(), prev.getLongitud(),
                        current.getLatitud(), current.getLongitud()
                );

                // velocidad en km/h
                double speedKmh = (distance / seconds) * 3.6;

                // filtrar velocidades
                if (speedKmh > 0 && speedKmh < 120) {
                    String archKey = arco.getLineId() + "-" +
                            arco.getOrientation() + "-" +
                            arco.getLineVariant();

                    archSpeedMap.computeIfAbsent(archKey, k -> new SpeedStats())
                            .addSpeed(speedKmh);

                    speedCalculated.incrementAndGet();

                    if (speedCalculated.get() <= 3) {
                        System.out.println(String.format(
                                "[Calculator] DEBUG Speed #%d: Bus %d, Arco %s, %.2f km/h (dist: %.0fm, tiempo: %ds)",
                                speedCalculated.get(), busId, archKey, speedKmh, distance, seconds
                        ));
                    }
                }
            }
        }

        lastPositionByBus.put(busId, current);
    }

    private void printFinalReport() {
        System.out.println("\n[Calculator] ═══════════════════════════════════════════════");
        System.out.println("[Calculator] ═══ REPORTE FINAL DE PROCESAMIENTO ═══");
        System.out.println("[Calculator] ═══════════════════════════════════════════════");
        System.out.println("[Calculator] ");
        System.out.println("[Calculator] Datagramas procesados:    " + String.format("%,10d", processed.get()));
        System.out.println("[Calculator] Matched con arco:         " + String.format("%,10d (%.1f%%)",
                matched.get(), (matched.get() * 100.0 / processed.get())));
        System.out.println("[Calculator] Velocidades calculadas:   " + String.format("%,10d", speedCalculated.get()));
        System.out.println("[Calculator] Sin match:                " + String.format("%,10d", validButNoMatch.get()));
        System.out.println("[Calculator] Coordenadas inválidas:    " + String.format("%,10d", invalidCoords.get()));
        System.out.println("[Calculator] ");
        System.out.println("[Calculator] ─────────────────────────────────────────────────");
        System.out.println("[Calculator] ESTADÍSTICAS DE VELOCIDAD POR ARCO");
        System.out.println("[Calculator] ─────────────────────────────────────────────────");
        System.out.println("[Calculator] Total de arcos únicos:    " + archSpeedMap.size());

        if (!archSpeedMap.isEmpty()) {
            System.out.println("[Calculator] ");
            System.out.println("[Calculator] Top 5 arcos más transitados:");
            System.out.println("[Calculator] lineId-orientation-variant");
            archSpeedMap.entrySet().stream()
                    .sorted((a, b) -> Integer.compare(b.getValue().getCount(), a.getValue().getCount()))
                    .limit(5)
                    .forEach(entry -> {
                        SpeedStats stats = entry.getValue();
                        System.out.println(String.format(
                                "[Calculator]   %s: %,d mediciones, vel. prom: %.2f km/h",
                                entry.getKey(), stats.getCount(), stats.getAverage()
                        ));
                    });

            double globalAvg = archSpeedMap.values().stream()
                    .mapToDouble(SpeedStats::getAverage)
                    .average()
                    .orElse(0.0);

            System.out.println("[Calculator] ");
            System.out.println(String.format(
                    "[Calculator] Velocidad promedio global: %.2f km/h",
                    globalAvg
            ));
        } else {
            System.out.println("[Calculator] ");
            System.out.println("[Calculator]     No se calcularon velocidades.");
            System.out.println("[Calculator]     Posibles causas:");
            System.out.println("[Calculator]     - Datagramas muy dispersos en tiempo");
            System.out.println("[Calculator]     - Pocos buses con múltiples posiciones");
            System.out.println("[Calculator]     - Delta de tiempo fuera de rango [1s, 300s]");
        }

        System.out.println("[Calculator] ═══════════════════════════════════════════════\n");
    }


    // Obtener resultados de velocidades

    public Map<String, SpeedStats> getArchSpeedMap() {
        return archSpeedMap;
    }

    public static class SpeedStats {
        private double totalSpeed = 0;
        private int count = 0;

        public synchronized void addSpeed(double speed) {
            totalSpeed += speed;
            count++;
        }

        public double getAverage() {
            return count > 0 ? totalSpeed / count : 0.0;
        }

        public int getCount() {
            return count;
        }
    }
}