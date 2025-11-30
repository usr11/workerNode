package org.example.engine;

import Demo.TaskResult;
import Demo.Worker;
import com.zeroc.Ice.Current;
import org.example.model.Datagram; // Asegúrate de que tu clase se llame así

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ProcessingEngine implements Worker {

    /**
     * Implementación del contrato definido en Compute.ice
     * Ahora aceptamos filePath + startOffset + endOffset
     */
    @Override
    public TaskResult processDatagramLog(String filePath, long startOffset, long endOffset, Current current) {

        System.out.println("--> [Engine] Iniciando tarea.");
        System.out.println("    Archivo: " + filePath);
        System.out.println("    Segmento: Bytes " + startOffset + " a " + endOffset);

        long start = System.currentTimeMillis();

        // 1. Crear la Cola (CalculationQueue)
        // Usamos ArrayBlockingQueue con capacidad limitada para manejar la contrapresión
        BlockingQueue<Datagram> queue = new ArrayBlockingQueue<>(5000);

        // 2. Instanciar Componentes Internos
        // NOTA: Por ahora el Reader lee todo el archivo.
        // En una fase futura de optimización, le pasaremos startOffset y endOffset al Reader.
        DatagramReader reader = new DatagramReader(filePath, startOffset, endOffset, queue);

        SpeedCalculator calculator = new SpeedCalculator(queue);

        // 3. Ejecutar Hilos
        Thread tReader = new Thread(reader, "Reader-Thread");
        Thread tCalc = new Thread(calculator, "Calc-Thread");

        tReader.start();
        tCalc.start();

        try {
            // Esperamos a que terminen (Barrier)
            tReader.join();
            tCalc.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long time = System.currentTimeMillis() - start;
        System.out.println("<-- [Engine] Tarea finalizada. Matched: " + calculator.matched.get());

        // 4. Retornar Resultado al Master
        // Calculamos porcentaje de éxito (Matched / Processed)
        double successRate = 0.0;
        int processed = calculator.processed.get();
        if (processed > 0) {
            successRate = ((double) calculator.matched.get() / processed) * 100.0;
        }

        // Obtenemos nombre de usuario de forma segura
        String workerName = System.getProperty("user.name");
        if (workerName == null) workerName = "Unknown";

        return new TaskResult(successRate, workerName, time);
    }
}