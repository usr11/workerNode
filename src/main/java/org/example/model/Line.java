package org.example.model;

import java.time.LocalDateTime;

public class Line {

    private int lineId;
    private int planVersionId;
    private String shortName;
    private String description;
    private LocalDateTime activationDate;

    public Line() {}

    public Line(int lineId, int planVersionId, String shortName, String description, LocalDateTime activationDate) {
        this.lineId = lineId;
        this.planVersionId = planVersionId;
        this.shortName = shortName;
        this.description = description;
        this.activationDate = activationDate;
    }

    public int getLineId() {
        return lineId;
    }

    public void setLineId(int lineId) {
        this.lineId = lineId;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getActivationDate() {
        return activationDate;
    }

    public void setActivationDate(LocalDateTime activationDate) {
        this.activationDate = activationDate;
    }
}
