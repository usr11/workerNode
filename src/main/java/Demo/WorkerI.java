package Demo;

import com.zeroc.Ice.Current;
import org.example.engine.ProcessingEngine; // Importar el motor real

public class WorkerI implements Demo.Worker {

    private final ProcessingEngine engine = new ProcessingEngine(); // Instancia del motor

    @Override
    public TaskResult processDatagramLog(String filePath, long startOffset, long endOffset, Current current) {

        System.out.println("Recibida tarea: " + filePath);
        System.out.println("Rango de bytes: " + startOffset + " -> " + endOffset);

        // --- CORRECCIÓN: Invocar el motor de procesamiento real ---
        return engine.processDatagramLog(filePath, startOffset, endOffset, current);
        // -------------------------------------------
    }
}