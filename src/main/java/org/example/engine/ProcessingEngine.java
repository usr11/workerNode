package org.example.engine;

import Demo.TaskResult;
import Demo.Worker;
import com.zeroc.Ice.Current;
import org.example.model.Datagram;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ProcessingEngine implements Worker {

    @Override
    public TaskResult processDatagramLog(String filePath, long startOffset, long endOffset, Current current) {

        System.out.println("\n╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║           NUEVA TAREA RECIBIDA DEL MASTER                 ║");
        System.out.println("╠═══════════════════════════════════════════════════════════╣");
        System.out.println("  Archivo:  " + filePath);
        System.out.println("  Segmento: [" + String.format("%,d", startOffset) + " - " + String.format("%,d", endOffset) + "] bytes");
        System.out.println("  Tamaño:   " + String.format("%,d", (endOffset - startOffset)) + " bytes (" +
                String.format("%.2f", (endOffset - startOffset) / 1024.0 / 1024.0) + " MB)");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
        System.out.println();

        long start = System.currentTimeMillis();

        // Cola de comunicación Producer-Consumer
        BlockingQueue<Datagram> queue = new ArrayBlockingQueue<>(1000);

        // Componentes del pipeline
        DatagramReader reader = new DatagramReader(filePath, startOffset, endOffset, queue);
        SpeedCalculator calculator = new SpeedCalculator(queue);

        // Iniciar hilos (Consumer primero para evitar deadlock)
        Thread tCalc = new Thread(calculator, "Calculator-Thread");
        Thread tReader = new Thread(reader, "Reader-Thread");

        tCalc.start();
        tReader.start();

        try {
            tReader.join();
            tCalc.join();
            System.out.println("[Engine] ✓ Ambos hilos finalizaron correctamente.");

        } catch (InterruptedException e) {
            System.err.println("[Engine] ✗ Interrupción detectada: " + e.getMessage());
            Thread.currentThread().interrupt();
        }

        long time = System.currentTimeMillis() - start;

        // Recolectar métricas
        int processed = calculator.processed.get();
        int matched = calculator.matched.get();
        int speedCalc = calculator.speedCalculated.get();

        // Calcular velocidad promedio de ESTE chunk
        double avgSpeed = 0.0;
        if (!calculator.getArchSpeedMap().isEmpty()) {
            avgSpeed = calculator.getArchSpeedMap().values().stream()
                    .mapToDouble(SpeedCalculator.SpeedStats::getAverage)
                    .average()
                    .orElse(0.0);
        }

        // Obtener Top 5 arcos para enviar al Master
        Demo.ArchSpeed[] topArches = calculator.getArchSpeedMap().entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue().getCount(), a.getValue().getCount()))
                .limit(5)
                .map(entry -> new Demo.ArchSpeed(
                        entry.getKey(),
                        entry.getValue().getAverage(),
                        entry.getValue().getCount()
                ))
                .toArray(Demo.ArchSpeed[]::new);

        // Resumen final del chunk
        System.out.println("\n╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║              TAREA COMPLETADA - RESUMEN                   ║");
        System.out.println("╠═══════════════════════════════════════════════════════════╣");
        System.out.println("  Datagramas procesados:   " + String.format("%,10d", processed));
        System.out.println("  Matched con arcos:       " + String.format("%,10d (%.1f%%)", matched,
                (processed > 0 ? matched * 100.0 / processed : 0)));
        System.out.println("  Velocidades calculadas:  " + String.format("%,10d", speedCalc));
        System.out.println("  Arcos únicos analizados: " + String.format("%,10d", calculator.getArchSpeedMap().size()));
        System.out.println("  Velocidad prom. chunk:   " + String.format("%10.2f km/h", avgSpeed));
        System.out.println("  ─────────────────────────────────────────────────────────");
        System.out.println("  Tiempo total:            " + String.format("%,10d ms", time));
        System.out.println("  Throughput:              " + String.format("%,10.0f datagramas/s",
                processed * 1000.0 / time));
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
        System.out.println();

        // Calcular tasa de éxito
        double successRate = processed > 0 ? (matched * 100.0 / processed) : 0.0;

        String workerName = System.getProperty("user.name");
        if (workerName == null) workerName = "Unknown";

        // ✅ Retornar TaskResult con velocidades incluidas
        return new TaskResult(
                successRate,      // value (match rate)
                workerName,       // workerName
                time,             // executionTime
                avgSpeed,         // globalAvgSpeed
                speedCalc,        // speedCount
                topArches         // topArches
        );
    }
}