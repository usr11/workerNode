package org.example.model;

public class Stop {

    private int stopId;
    private int planVersionId;
    private String shortName;
    private String longName;
    private int gpsX;
    private int gpsY;
    private double decimalLong;
    private double decimalLatitude;

    public Stop() {}

    public Stop(int stopId, int planVersionId, String shortName, String longName, int gpsX, int gpsY, double decimalLong, double decimalLatitude) {
        this.stopId = stopId;
        this.planVersionId = planVersionId;
        this.shortName = shortName;
        this.longName = longName;
        this.gpsX = gpsX;
        this.gpsY = gpsY;
        this.decimalLong = decimalLong;
        this.decimalLatitude = decimalLatitude;
    }

    public int getStopId() {
        return stopId;
    }

    public void setStopId(int stopId) {
        this.stopId = stopId;
    }

    public int getPlanVersionId() {
        return planVersionId;
    }

    public void setPlanVersionId(int planVersionId) {
        this.planVersionId = planVersionId;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getLongName() {
        return longName;
    }

    public void setLongName(String longName) {
        this.longName = longName;
    }

    public int getGpsX() {
        return gpsX;
    }

    public void setGpsX(int gpsX) {
        this.gpsX = gpsX;
    }

    public int getGpsY() {
        return gpsY;
    }

    public void setGpsY(int gpsY) {
        this.gpsY = gpsY;
    }

    public double getDecimalLong() {
        return decimalLong;
    }

    public void setDecimalLong(double decimalLong) {
        this.decimalLong = decimalLong;
    }

    public double getDecimalLatitude() {
        return decimalLatitude;
    }

    public void setDecimalLatitude(double decimalLatitude) {
        this.decimalLatitude = decimalLatitude;
    }
}
