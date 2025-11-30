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

        System.out.println("--> [Engine] Iniciando tarea.");
        System.out.println("    Archivo: " + filePath);
        System.out.println("    Segmento: Bytes " + startOffset + " a " + endOffset);

        long start = System.currentTimeMillis();

        // 1. Crear la Cola (Producer-Consumer Pattern)
        // AJUSTE: Reducir capacidad para evitar overhead de memoria
        BlockingQueue<Datagram> queue = new ArrayBlockingQueue<>(1000);

        // 2. Instanciar Componentes
        DatagramReader reader = new DatagramReader(filePath, startOffset, endOffset, queue);
        SpeedCalculator calculator = new SpeedCalculator(queue);

        // 3. CRÍTICO: Iniciar Calculator PRIMERO (Consumer antes que Producer)
        Thread tCalc = new Thread(calculator, "Calculator-Thread");
        Thread tReader = new Thread(reader, "Reader-Thread");

        // ✅ ORDEN CORRECTO: Consumer primero
        tCalc.start();
        tReader.start();

        try {
            // ✅ Esperar a AMBOS sin importar el orden
            // No hay dependencia: ambos corren en paralelo
            tReader.join();
            tCalc.join();

            System.out.println("[Engine] Ambos hilos terminaron correctamente.");

        } catch (InterruptedException e) {
            System.err.println("[Engine] Interrupción detectada.");
            e.printStackTrace();
            // Propagar la interrupción
            Thread.currentThread().interrupt();
        }

        long time = System.currentTimeMillis() - start;

        // 4. Calcular métricas
        int processed = calculator.processed.get();
        int matched = calculator.matched.get();

        System.out.println("<-- [Engine] Tarea finalizada.");
        System.out.println("    Datagramas procesados: " + processed);
        System.out.println("    Datagramas matched: " + matched);
        System.out.println("    Tiempo: " + time + " ms");

        double successRate = 0.0;
        if (processed > 0) {
            successRate = ((double) matched / processed) * 100.0;
        }

        String workerName = System.getProperty("user.name");
        if (workerName == null) workerName = "Unknown";

        return new TaskResult(successRate, workerName, time);
    }
}