package com.watering.model;

public class Plant {

    private double soilMoisture;
    private double lastWatered;
    private int plantType;
    private int actualNeedsWater;

    private double x;
    private double y;
    private boolean placed;

    private int predictedNeedsWater;

    public Plant(double soilMoisture, double lastWatered,
                 int plantType, int actualNeedsWater) {
        this.soilMoisture = soilMoisture;
        this.lastWatered = lastWatered;
        this.plantType = plantType;
        this.actualNeedsWater = actualNeedsWater;
        this.predictedNeedsWater = -1;
        this.x = 0;
        this.y = 0;
        this.placed = false;
    }

    public double getSoilMoisture() {
        return soilMoisture;
    }

    public double getLastWatered() {
        return lastWatered;
    }

    public int getPlantType() {
        return plantType;
    }

    public int getActualNeedsWater() {
        return actualNeedsWater;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public boolean isPlaced() {
        return placed;
    }

    public int getPredictedNeedsWater() {
        return predictedNeedsWater;
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
        this.placed = true;
    }

    public void clearPosition() {
        this.x = 0;
        this.y = 0;
        this.placed = false;
    }

    public void setPredictedNeedsWater(int predicted) {
        this.predictedNeedsWater = predicted;
    }

    public String getPlantTypeName() {
        return switch (plantType) {
            case 0 -> "Cactus";
            case 1 -> "Flower";
            case 2 -> "Herb";
            default -> "Unknown";
        };
    }

    public String getPlantTypeShort() {
        return switch (plantType) {
            case 0 -> "C";
            case 1 -> "F";
            case 2 -> "H";
            default -> "?";
        };
    }

    @Override
    public String toString() {
        return getPlantTypeName()
                + " | moisture=" + soilMoisture
                + " | lastWatered=" + lastWatered
                + (placed ? " | placed" : " | not placed");
    }
}