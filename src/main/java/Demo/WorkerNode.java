package Demo;

import com.zeroc.Ice.*;

public class WorkerNode {
    public static void main(String[] args) {
        try(Communicator communicator = Util.initialize(args, "config.worker")) {

            ObjectAdapter adapter = communicator.createObjectAdapter("WorkerAdapter");

            com.zeroc.Ice.Object workerImpl = new WorkerI();
            adapter.add(workerImpl, Util.stringToIdentity("SimpleWorker"));
            adapter.activate();

            System.out.println("WorkerNode Listo. Grafo se cargará en la primera petición.");

            communicator.waitForShutdown();
        }
    }
}