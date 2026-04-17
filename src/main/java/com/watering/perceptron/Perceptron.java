package com.watering.perceptron;

import com.watering.model.Plant;
import javax.swing.JTextArea;
import java.util.ArrayList;
import java.util.List;

public class Perceptron {

    private double learningRate;
    private double w1, w2, w3;
    private double threshold;

    // ── holds per-epoch stats for the learning curve chart ──────────────────
    public static class EpochRecord {
        public final int epoch;
        public final int errors;
        public final double accuracy;

        public EpochRecord(int epoch, int errors, double accuracy) {
            this.epoch    = epoch;
            this.errors   = errors;
            this.accuracy = accuracy;
        }
    }

    public Perceptron(double learningRate) {
        this.learningRate = learningRate;
        this.w1 = Math.random() - 0.5;
        this.w2 = Math.random() - 0.5;
        this.w3 = Math.random() - 0.5;
        this.threshold = Math.random() - 0.5;
    }

    private int stepFunction(double sum) {
        return (sum >= 0) ? 1 : 0;
    }

    private double[] normalize(double soilMoisture,
                               double lastWatered, int plantType) {
        return new double[]{
                soilMoisture / 100.0,
                lastWatered  / 48.0,
                plantType    / 2.0
        };
    }

    public int predict(double soilMoisture,
                       double lastWatered, int plantType) {
        double[] x = normalize(soilMoisture, lastWatered, plantType);
        double sum = x[0]*w1 + x[1]*w2 + x[2]*w3 - threshold;
        return stepFunction(sum);
    }

    // ── original train (writes to log area only) ─────────────────────────────
    public void train(List<Plant> plants, int epochs, JTextArea output) {
        for (int epoch = 1; epoch <= epochs; epoch++) {
            int errors = 0;

            for (Plant plant : plants) {
                int predicted = predict(plant.getSoilMoisture(),
                        plant.getLastWatered(),
                        plant.getPlantType());

                int error = plant.getActualNeedsWater() - predicted;

                if (error != 0) {
                    errors++;
                    double[] x = normalize(plant.getSoilMoisture(),
                            plant.getLastWatered(),
                            plant.getPlantType());
                    w1 += learningRate * x[0] * error;
                    w2 += learningRate * x[1] * error;
                    w3 += learningRate * x[2] * error;
                    threshold -= learningRate * error;
                }
            }

            if (epoch % 10 == 0 || epoch == 1) {
                output.append("Epoch " + epoch +
                        " | Errors: " + errors + "\n");
            }

            if (errors == 0) {
                output.append("Converged at epoch " + epoch + "!\n");
                break;
            }
        }
    }

    // ── new: train AND collect epoch history for the chart ───────────────────
    public List<EpochRecord> trainWithHistory(List<Plant> plants, int epochs,
                                              JTextArea output) {
        List<EpochRecord> history = new ArrayList<>();

        for (int epoch = 1; epoch <= epochs; epoch++) {
            int errors = 0;

            for (Plant plant : plants) {
                int predicted = predict(plant.getSoilMoisture(),
                        plant.getLastWatered(),
                        plant.getPlantType());

                int error = plant.getActualNeedsWater() - predicted;

                if (error != 0) {
                    errors++;
                    double[] x = normalize(plant.getSoilMoisture(),
                            plant.getLastWatered(),
                            plant.getPlantType());
                    w1 += learningRate * x[0] * error;
                    w2 += learningRate * x[1] * error;
                    w3 += learningRate * x[2] * error;
                    threshold -= learningRate * error;
                }
            }

            double acc = calculateAccuracy(plants);
            history.add(new EpochRecord(epoch, errors, acc));

            if (epoch % 10 == 0 || epoch == 1) {
                output.append("Epoch " + epoch + " | Errors: " + errors
                        + " | Accuracy: "
                        + String.format(java.util.Locale.US, "%.1f", acc) + "%\n");
            }

            if (errors == 0) {
                output.append("Converged at epoch " + epoch + "!\n");
                break;
            }
        }
        return history;
    }

    public void predictAll(List<Plant> plants) {
        for (Plant plant : plants) {
            int result = predict(plant.getSoilMoisture(),
                    plant.getLastWatered(),
                    plant.getPlantType());
            plant.setPredictedNeedsWater(result);
        }
    }

    public double calculateAccuracy(List<Plant> plants) {
        int correct = 0;
        for (Plant plant : plants) {
            int predicted = predict(plant.getSoilMoisture(),
                    plant.getLastWatered(),
                    plant.getPlantType());
            if (predicted == plant.getActualNeedsWater()) correct++;
        }
        return (double) correct / plants.size() * 100.0;
    }
}