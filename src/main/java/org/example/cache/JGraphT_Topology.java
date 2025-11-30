package org.example.cache;

import org.example.csv.LineStopLoader;
import org.example.csv.StopLoader;
import org.example.graph.Arch;
import org.example.graph.GraphBuilder;
import org.example.logic.GeoUtils;
import org.example.model.LineStop;
import org.example.model.Stop;
import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import java.util.List;

public class JGraphT_Topology {

    private static JGraphT_Topology instance;
    private Graph<Stop, Arch> graph;

    private JGraphT_Topology() {
        System.out.println("[Cache] Inicializando Singleton de Topología...");
        try {
            List<Stop> stops = StopLoader.load("stops.csv");
            List<LineStop> lineStops = LineStopLoader.load("linestops.csv");
            this.graph = GraphBuilder.buildGraph(stops, lineStops);
            System.out.println("[Cache] Grafo cargado: " + graph.edgeSet().size() + " arcos.");
        } catch (Exception e) {
            e.printStackTrace();
            this.graph = new DirectedWeightedMultigraph<>(Arch.class);
        }
    }

    public static synchronized JGraphT_Topology getInstance() {
        if (instance == null) {
            instance = new JGraphT_Topology();
        }
        return instance;
    }

    public Arch findNearestArch(double lat, double lon) {
        Arch best = null;
        double minDst = 50.0; // Umbral de 50 metros

        // OPTIMIZACIÓN: Umbral de "caja" en grados lat/lon.
        // 0.005 grados son aprox 500 metros.
        // Si el arco está a más de 500m en línea recta simple, ni siquiera calculamos trigonometría.
        double BOX_SIZE = 0.005;

        for (Arch edge : graph.edgeSet()) {
            Stop s1 = graph.getEdgeSource(edge);
            Stop s2 = graph.getEdgeTarget(edge);

            double midLat = (s1.getDecimalLatitude() + s2.getDecimalLatitude()) / 2;
            double midLon = (s1.getDecimalLong() + s2.getDecimalLong()) / 2;

            // 1. FILTRO RÁPIDO (Bounding Box) - Restas simples, muy barato para el CPU
            if (Math.abs(lat - midLat) > BOX_SIZE || Math.abs(lon - midLon) > BOX_SIZE) {
                continue; // Saltar este arco, está muy lejos
            }

            // 2. CÁLCULO PRECISO (Solo si pasó el filtro)
            double d = GeoUtils.haversine(lat, lon, midLat, midLon);
            if (d < minDst) {
                minDst = d;
                best = edge;
            }
        }
        return best;
    }
}