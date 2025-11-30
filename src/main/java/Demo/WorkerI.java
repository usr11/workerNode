package Demo;

import com.zeroc.Ice.Current;

public class WorkerI implements Demo.Worker {

    // ANTES: public TaskResult executeTask(String taskData, Current current)
    // AHORA: Debes usar el nombre y los argumentos nuevos definidos en el .ice
    @Override
    public TaskResult processDatagramLog(String filePath, long startOffset, long endOffset, Current current) {

        System.out.println("Recibida tarea: " + filePath);
        System.out.println("Rango de bytes: " + startOffset + " -> " + endOffset);

        long start = System.currentTimeMillis();

        // --- AQUÍ IRÁ TU LÓGICA DE PROCESAMIENTO ---
        // (Por ahora dejamos la simulación para que veas que compila)
        double result = 0;
        try {
            Thread.sleep(500);
            result = Math.random() * 100;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // -------------------------------------------

        long time = System.currentTimeMillis() - start;

        // Obtenemos nombre de usuario de forma segura
        String workerName = System.getProperty("user.name");
        if (workerName == null) workerName = "Unknown";

        return new TaskResult(result, workerName, time);
    }
}