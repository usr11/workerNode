package org.example.graph;

import org.example.model.Stop;
import org.jgrapht.Graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphPrinter {

    public static void printByLines(Graph<Stop, Arch> graph) {


        System.out.println("\n\n\n\n╔═════════════════════╗");
        System.out.println("║    Arcos por ruta   ║");
        System.out.println("╚═════════════════════╝");

        Map<String, List<Arch>> byLine = new HashMap<>();

        for (Arch edge : graph.edgeSet()) {

            String key = edge.getLineId() + "-" +
                    edge.getOrientation() + "-" +
                    edge.getLineVariant();

            byLine.computeIfAbsent(key, k -> new ArrayList<>())
                    .add(edge);
        }

        // imprimir datos agrupados
        for (String key : byLine.keySet()) {

            List<Arch> edges = byLine.get(key);


            // extraer datos de la linea
            Arch sample = edges.get(0);


            System.out.println(
                    "\n\n======= Línea: " + sample.getLineId() +
                            " | Orientacion: " + sample.getOrientation() +
                            " | Variante: " + sample.getLineVariant() +
                            " =======\n"
            );

            for (Arch e : edges) {
                Stop from = graph.getEdgeSource(e);
                Stop to = graph.getEdgeTarget(e);
                System.out.printf(
                        "ID: %-5d | Nombre Corto: %-10s --->    ID: %-5d | Nombre Corto: %-20s%n",
                        from.getStopId(),
                        from.getShortName(),
                        to.getStopId(),
                        to.getShortName()
                );
            }
        }

        System.out.println();
    }
}
