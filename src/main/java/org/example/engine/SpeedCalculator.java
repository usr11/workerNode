package org.example.engine;

import org.example.cache.JGraphT_Topology;
import org.example.graph.Arch;
import org.example.model.Datagram;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class SpeedCalculator implements Runnable {

    private final BlockingQueue<Datagram> queue;
    private final JGraphT_Topology topology;

    // Contadores
    public final AtomicInteger processed = new AtomicInteger(0);
    public final AtomicInteger matched = new AtomicInteger(0);
    public final AtomicInteger invalidCoords = new AtomicInteger(0);
    public final AtomicInteger validButNoMatch = new AtomicInteger(0);

    public SpeedCalculator(BlockingQueue<Datagram> queue) {
        this.queue = queue;
        this.topology = JGraphT_Topology.getInstance();
    }

    @Override
    public void run() {
        System.out.println("[Calculator] === INICIANDO PROCESAMIENTO ===");
        System.out.println("[Calculator] Esperando datagramas...");

        long lastReport = System.currentTimeMillis();
        int lastProcessed = 0;

        try {
            while (true) {
                Datagram d = queue.take();

                // Verificar POISON_PILL
                if (d.isPoisonPill()) {
                    System.out.println("[Calculator] POISON_PILL recibida. Terminando.");
                    break;
                }

                // Procesar datagrama
                try {
                    // Validar coordenadas usando el método del modelo
                    if (!d.hasValidCoordinates()) {
                        invalidCoords.incrementAndGet();
                        processed.incrementAndGet();
                        continue;
                    }

                    // Buscar arco más cercano
                    Arch arco = topology.findNearestArch(d.getLatitud(), d.getLongitud());

                    if (arco != null) {
                        matched.incrementAndGet();
                        // Aquí iría el cálculo de velocidad si lo implementas
                        // Por ahora solo contamos matches
                    } else {
                        validButNoMatch.incrementAndGet();
                    }

                    int total = processed.incrementAndGet();

                    // Reporte cada segundo
                    long now = System.currentTimeMillis();
                    if (now - lastReport >= 1000) {
                        int rate = total - lastProcessed;
                        System.out.println(String.format(
                                "[Calculator] Total: %,7d | Match: %,7d (%.1f%%) | NoMatch: %,d | Invalid: %,d | Rate: %,d/s | Queue: %d",
                                total,
                                matched.get(),
                                (matched.get() * 100.0 / total),
                                validButNoMatch.get(),
                                invalidCoords.get(),
                                rate,
                                queue.size()
                        ));
                        lastReport = now;
                        lastProcessed = total;
                    }

                } catch (Exception e) {
                    System.err.println("[Calculator] Error procesando datagrama: " + e.getMessage());
                    if (processed.get() < 10) {
                        e.printStackTrace(); // Solo mostrar stack trace para los primeros errores
                    }
                    processed.incrementAndGet();
                }
            }

            System.out.println("\n[Calculator] === PROCESAMIENTO COMPLETADO ===");
            System.out.println("[Calculator] Total procesados:     " + String.format("%,d", processed.get()));
            System.out.println("[Calculator] Matched (con arco):   " + String.format("%,d", matched.get()));
            System.out.println("[Calculator] Válidos sin match:    " + String.format("%,d", validButNoMatch.get()));
            System.out.println("[Calculator] Coordenadas inválidas: " + String.format("%,d", invalidCoords.get()));

            double matchRate = processed.get() > 0
                    ? (matched.get() * 100.0 / processed.get())
                    : 0.0;
            System.out.println(String.format("[Calculator] Tasa de éxito: %.2f%%", matchRate));

        } catch (InterruptedException e) {
            System.err.println("[Calculator] Hilo interrumpido.");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println("[Calculator] ERROR CRÍTICO: " + e.getMessage());
            e.printStackTrace();
        }
    }
}