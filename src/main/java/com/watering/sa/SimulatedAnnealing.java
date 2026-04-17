package com.watering.sa;

import com.watering.model.Plant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SimulatedAnnealing {

    private static final double MISSED_PENALTY = 100.0;
    private static final double EXTRA_PENALTY  = 50.0;

    private double temperature;
    private double coolingRate;
    private Random random;

    // ── step record for visualization ────────────────────────────────────
    public static class StepRecord {
        public final int    step;
        public final double cost;
        public final double temperature;
        public final int    totalSwaps;
        public final boolean accepted;
        public final List<String> currentOrder;  // ← جديد

        public StepRecord(int step, double cost, double temperature,
                          int totalSwaps, boolean accepted, List<String> currentOrder) {
            this.step         = step;
            this.cost         = cost;
            this.temperature  = temperature;
            this.totalSwaps   = totalSwaps;
            this.accepted     = accepted;
            this.currentOrder = currentOrder;
        }
    }

    public SimulatedAnnealing(double temperature, double coolingRate) {
        this.temperature = temperature;
        this.coolingRate = coolingRate;
        this.random      = new Random();
    }

    public double distance(Plant a, Plant b) {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    public double calculateCost(List<Plant> sequence,
                                List<Plant> allPlantsThatNeedWater) {
        if (sequence == null || sequence.isEmpty()) return Double.MAX_VALUE;

        double totalDistance = 0.0;
        for (int i = 0; i < sequence.size() - 1; i++)
            totalDistance += distance(sequence.get(i), sequence.get(i + 1));

        int missed = 0;
        for (Plant p : allPlantsThatNeedWater)
            if (!sequence.contains(p)) missed++;

        int extra = 0;
        for (Plant p : sequence)
            if (p.getPredictedNeedsWater() == 0) extra++;

        return (missed * MISSED_PENALTY) + totalDistance + (extra * EXTRA_PENALTY);
    }

    private List<Plant> swapPlants(List<Plant> sequence) {
        if (sequence.size() < 2) return new ArrayList<>(sequence);

        List<Plant> next = new ArrayList<>(sequence);
        int i = random.nextInt(next.size());
        int j = random.nextInt(next.size());
        while (i == j) j = random.nextInt(next.size());
        Collections.swap(next, i, j);
        return next;
    }

    // ── optimize WITH history ─────────────────────────────────────────────
    public List<Plant> optimize(List<Plant> selectedPlantsToWater,
                                List<Plant> allPlantsThatNeedWater) {
        if (selectedPlantsToWater == null || selectedPlantsToWater.isEmpty())
            return new ArrayList<>();

        List<Plant> currentSolution = new ArrayList<>(selectedPlantsToWater);
        Collections.shuffle(currentSolution);
        List<Plant> bestSolution = new ArrayList<>(currentSolution);

        double currentCost        = calculateCost(currentSolution, allPlantsThatNeedWater);
        double bestCost           = currentCost;
        double currentTemperature = temperature;

        while (currentTemperature > 1) {
            List<Plant> newSolution = swapPlants(currentSolution);
            double      newCost     = calculateCost(newSolution, allPlantsThatNeedWater);

            if (acceptanceProbability(currentCost, newCost, currentTemperature)
                    > random.nextDouble()) {
                currentSolution = new ArrayList<>(newSolution);
                currentCost     = newCost;
            }

            if (currentCost < bestCost) {
                bestSolution = new ArrayList<>(currentSolution);
                bestCost     = currentCost;
            }

            currentTemperature *= (1 - coolingRate);
        }

        return bestSolution;
    }

    // ── optimize AND collect step history ─────────────────────────────────
    public List<Plant> optimizeWithHistory(List<Plant> selectedPlantsToWater,
                                           List<Plant> allPlantsThatNeedWater,
                                           List<StepRecord> history) {
        if (selectedPlantsToWater == null || selectedPlantsToWater.isEmpty())
            return new ArrayList<>();

        history.clear();

        List<Plant> currentSolution = new ArrayList<>(selectedPlantsToWater);
        Collections.shuffle(currentSolution);
        List<Plant> bestSolution = new ArrayList<>(currentSolution);

        double currentCost        = calculateCost(currentSolution, allPlantsThatNeedWater);
        double bestCost           = currentCost;
        double currentTemperature = temperature;

        int step       = 0;
        int totalSwaps = 0;

        // record every 10th step to keep chart readable (max ~1000 points)
        final int SAMPLE = 10;

        while (currentTemperature > 1) {
            step++;

            List<Plant> newSolution = swapPlants(currentSolution);
            double      newCost     = calculateCost(newSolution, allPlantsThatNeedWater);

            double prob    = acceptanceProbability(currentCost, newCost, currentTemperature);
            boolean accept = prob > random.nextDouble();

            if (accept) {
                currentSolution = new ArrayList<>(newSolution);
                currentCost     = newCost;
                totalSwaps++;
            }

            if (currentCost < bestCost) {
                bestSolution = new ArrayList<>(currentSolution);
                bestCost     = currentCost;
            }

            if (step % SAMPLE == 0) {
                List<String> order = new ArrayList<>();
                for (Plant p : currentSolution)
                    order.add(p.getPlantTypeShort());

                history.add(new StepRecord(step, currentCost,
                        currentTemperature, totalSwaps, accept, order));
            }

            currentTemperature *= (1 - coolingRate);
        }

        // always add final point
        List<String> finalOrder = new ArrayList<>();
        for (Plant p : bestSolution)
            finalOrder.add(p.getPlantTypeShort());

        history.add(new StepRecord(step, bestCost,
                currentTemperature, totalSwaps, true, finalOrder));
        return bestSolution;
    }

    private double acceptanceProbability(double currentCost, double newCost,
                                         double temperature) {
        if (newCost < currentCost) return 1.0;
        return Math.exp((currentCost - newCost) / temperature);
    }
}