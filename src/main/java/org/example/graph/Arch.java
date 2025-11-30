package org.example.graph;

import org.jgrapht.graph.DefaultWeightedEdge;

public class Arch extends DefaultWeightedEdge {

    private int lineId;
    private int orientation;
    private int lineVariant;

    public Arch() {}

    public Arch(int lineId, int orientation, int lineVariant) {
        this.lineId = lineId;
        this.orientation = orientation;
        this.lineVariant = lineVariant;
    }

    public void setLineId(int lineId) { this.lineId = lineId; }
    public void setOrientation(int orientation) { this.orientation = orientation; }
    public void setLineVariant(int lineVariant) { this.lineVariant = lineVariant; }

    public int getLineId() { return lineId; }
    public int getOrientation() { return orientation; }
    public int getLineVariant() { return lineVariant; }

    public String getRouteKey() {
        return lineId + "-" + orientation + "-" + lineVariant;
    }

    @Override
    public String toString() {
        return "Línea " + lineId + " (Orient:" + orientation + ", Var:" + lineVariant + ")";
    }
}