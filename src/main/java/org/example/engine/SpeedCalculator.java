package org.example.engine;

import org.example.cache.JGraphT_Topology;
import org.example.graph.Arch;
import org.example.model.Datagram;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class SpeedCalculator implements Runnable {

    private final BlockingQueue<Datagram> queue;
    private final JGraphT_Topology topology;

    // Contadores atómicos para métricas thread-safe
    public final AtomicInteger processed = new AtomicInteger(0);
    public final AtomicInteger matched = new AtomicInteger(0);

    public SpeedCalculator(BlockingQueue<Datagram> queue) {
        this.queue = queue;
        this.topology = JGraphT_Topology.getInstance(); // Acceso al Artefacto
    }

    @Override
    public void run() {
        try {
            while (true) {
                Datagram d = queue.take(); // Espera dato

                if (d.isPoisonPill()) {
                    break; // Fin del trabajo
                }

                // Usamos la topología para buscar el arco
                Arch arco = topology.findNearestArch(d.getLatitud(), d.getLongitud());

                if (arco != null) {
                    matched.incrementAndGet();
                    // Aquí iría la lógica de cálculo de velocidad (Delta T)
                    // System.out.println("Bus " + d.getBusId() + " en arco variante " + arco.getLineVariant());
                }

                int total = processed.incrementAndGet();

                if (total % 100 == 0) {
                    System.out.println("[Calculator] Procesados: " + total + " | Matched: " + matched.get());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}