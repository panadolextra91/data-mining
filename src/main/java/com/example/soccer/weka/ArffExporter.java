package com.example.soccer.weka;

import com.example.soccer.db.MatchRecord;
import com.example.soccer.feature.FeatureBuilder;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Export engineered features + labels to ARFF for Weka.
 * Currently exports the Combined feature set (23 attributes).
 */
public final class ArffExporter {

    private ArffExporter() {
    }

    public static void exportCombinedToArff(List<MatchRecord> records, String outputPath) throws IOException {
        double[][] features = FeatureBuilder.buildCombinedFeatures(records);
        int[] labels = FeatureBuilder.buildLabels(records);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            writer.write("@relation soccer_combined\n\n");

            int numFeatures = features[0].length;
            for (int i = 0; i < numFeatures; i++) {
                writer.write(String.format("@attribute x%d numeric%n", i));
            }
            writer.write("@attribute class {home,draw,away}\n\n");

            writer.write("@data\n");
            for (int i = 0; i < features.length; i++) {
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < numFeatures; j++) {
                    if (j > 0) sb.append(',');
                    sb.append(features[i][j]);
                }
                sb.append(',');
                int y = labels[i];
                if (y == 0) {
                    sb.append("home");
                } else if (y == 1) {
                    sb.append("draw");
                } else {
                    sb.append("away");
                }
                writer.write(sb.toString());
                writer.newLine();
            }
        }
    }
}



