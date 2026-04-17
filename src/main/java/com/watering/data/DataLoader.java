package com.watering.data;

import com.watering.model.Plant;
import java.io.*;
import java.util.*;

public class DataLoader {

    public static List<Plant> loadPlantsFromCSV(String filename)
    {
        List<Plant> plants = new ArrayList<>();

        InputStream is = DataLoader.class
                .getClassLoader()
                .getResourceAsStream(filename);

        if (is == null)
        {
            System.out.println("File not found: " + filename);
            return plants;
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is)))
        {
            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {
                if (firstLine) { firstLine = false; continue; }

                String[] parts = line.trim().split("[,\\t]+");
                if (parts.length < 4) continue;

                double soilMoisture = Double.parseDouble(parts[0]);
                double lastWatered  = Double.parseDouble(parts[1]);
                int plantType       = Integer.parseInt(parts[2]);
                int needsWater      = Integer.parseInt(parts[3]);

                plants.add(new Plant(soilMoisture, lastWatered,
                        plantType, needsWater));
            }
        } catch (Exception e) {
            System.out.println("Error reading file: " + e.getMessage());
        }

        return plants;
    }
}