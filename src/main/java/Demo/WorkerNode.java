package Demo;

import com.zeroc.Ice.*;
import org.example.engine.ProcessingEngine;

public class WorkerNode {
    public static void main(String[] args) {

        String configFile = (args.length > 0) ? args[0] : "config.worker";

        try(Communicator communicator = Util.initialize(args, configFile)) {

            ObjectAdapter adapter = communicator.createObjectAdapter("WorkerAdapter");

            com.zeroc.Ice.Object engine = new ProcessingEngine();
            adapter.add(engine, Util.stringToIdentity("SimpleWorker"));
            adapter.activate();

            System.out.println("WorkerNode Listo. Grafo se cargará en la primera petición.");
            System.out.println("Worker iniciado con " + configFile);

            communicator.waitForShutdown();
        }
    }
}