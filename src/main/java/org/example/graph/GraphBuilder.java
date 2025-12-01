package org.example.graph;

import org.example.model.LineStop;
import org.example.model.Stop;
import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import java.util.*;

public class GraphBuilder {

    public static Graph<Stop, Arch> buildGraph(
            List<Stop> stops,
            List<LineStop> lineStops
    ) {

        Graph<Stop, Arch> graph =
                new DirectedWeightedMultigraph<>(Arch.class);


        Map<Integer, Stop> stopMap = new HashMap<>();
        for (Stop s : stops) {
            stopMap.put(s.getStopId(), s);
            graph.addVertex(s);
        }

        Map<String, List<LineStop>> lines = new HashMap<>();

        for (LineStop ls : lineStops) {
            String key = ls.getLineId() + "-" + ls.getOrientation() + "-" + ls.getLineVariant();
            lines.computeIfAbsent(key, k -> new ArrayList<>()).add(ls);
        }

        for (Map.Entry<String, List<LineStop>> entry : lines.entrySet()) {

            List<LineStop> list = entry.getValue();
            list.sort(Comparator.comparingInt(LineStop::getStopSequence));

            LineStop first = list.get(0);
            int lineId = first.getLineId();
            int orientation = first.getOrientation();
            int lineVariant = first.getLineVariant();

            for (int i = 0; i < list.size() - 1; i++) {

                Stop from = stopMap.get(list.get(i).getStopId());
                Stop to = stopMap.get(list.get(i + 1).getStopId());

                if (from != null && to != null) {
                    Arch edge = new Arch(lineId, orientation, lineVariant);
                    edge = graph.addEdge(from, to);

                    if (edge != null) {
                        edge.setLineId(lineId);
                        edge.setOrientation(orientation);
                        edge.setLineVariant(lineVariant);
                        graph.setEdgeWeight(edge, 1);
                    }
                }
            }
        }

        return graph;
    }
}
