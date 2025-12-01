package Demo;

import com.zeroc.Ice.*;

import java.lang.Exception;
import java.net.InetAddress;

public class WorkerNode {
    public static void main(String[] args) {

        printBanner();

        try(Communicator communicator = Util.initialize(args, "config.worker")) {

            ObjectAdapter adapter = communicator.createObjectAdapter("WorkerAdapter");

            com.zeroc.Ice.Object workerImpl = new WorkerI();
            adapter.add(workerImpl, Util.stringToIdentity("SimpleWorker"));
            adapter.activate();

            String hostName = getHostInfo();
            String endpoint = adapter.getEndpoints()[0].toString();

            System.out.println("╔════════════════════════════════════════════════════════════╗");
            System.out.println("║              ✓ WORKER NODE ACTIVO                          ║");
            System.out.println("╠════════════════════════════════════════════════════════════╣");
            System.out.println("  Host:     " + hostName);
            System.out.println("  Endpoint: " + endpoint);
            System.out.println("  Usuario:  " + System.getProperty("user.name"));
            System.out.println("  JVM:      " + System.getProperty("java.version"));
            System.out.println("╠════════════════════════════════════════════════════════════╣");
            System.out.println("  Estado:   Esperando tareas del Master...");
            System.out.println("  Nota:     El grafo se cargará al recibir la primera tarea");
            System.out.println("╚════════════════════════════════════════════════════════════╝");
            System.out.println();

            communicator.waitForShutdown();

            System.out.println("\n[Worker] Apagado graceful. Adiós.");

        } catch (Exception e) {
            System.err.println("[Worker] ERROR FATAL: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void printBanner() {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                   WORKER NODE - MIO System                   ║");
        System.out.println("║           Sistema Distribuido de Análisis de Tráfico        ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();
    }

    private static String getHostInfo() {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            return addr.getHostName() + " (" + addr.getHostAddress() + ")";
        } catch (Exception e) {
            return "localhost";
        }
    }
}