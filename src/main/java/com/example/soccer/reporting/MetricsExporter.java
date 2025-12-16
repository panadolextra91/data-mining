package com.example.soccer.reporting;

import com.example.soccer.model.ClassificationMetrics;
import com.example.soccer.model.TrainedModel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Export per-model metrics to CSV for external analysis and plotting.
 */
public final class MetricsExporter {

    private MetricsExporter() {
    }

    public static synchronized void appendMetrics(String path, TrainedModel model) {
        ClassificationMetrics m = model.getMetrics();
        double acc = m.getAccuracy();
        double[] prec = m.getPrecision();
        double[] rec = m.getRecall();
        double[] f1 = m.getF1Score();
        double[][] cm = m.getConfusionMatrix();

        File file = new File(path);
        boolean writeHeader = !file.exists();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            if (writeHeader) {
                writer.write(String.join(",",
                        "modelType",
                        "featureSet",
                        "accuracy",
                        "precision_home", "recall_home", "f1_home",
                        "precision_draw", "recall_draw", "f1_draw",
                        "precision_away", "recall_away", "f1_away",
                        "cm_hh", "cm_hd", "cm_ha",
                        "cm_dh", "cm_dd", "cm_da",
                        "cm_ah", "cm_ad", "cm_aa"));
                writer.newLine();
            }

            String line = String.join(",",
                    model.getModelType(),
                    model.getFeatureSetName(),
                    String.valueOf(acc),
                    String.valueOf(prec[0]), String.valueOf(rec[0]), String.valueOf(f1[0]),
                    String.valueOf(prec[1]), String.valueOf(rec[1]), String.valueOf(f1[1]),
                    String.valueOf(prec[2]), String.valueOf(rec[2]), String.valueOf(f1[2]),
                    String.valueOf(cm[0][0]), String.valueOf(cm[0][1]), String.valueOf(cm[0][2]),
                    String.valueOf(cm[1][0]), String.valueOf(cm[1][1]), String.valueOf(cm[1][2]),
                    String.valueOf(cm[2][0]), String.valueOf(cm[2][1]), String.valueOf(cm[2][2]));

            writer.write(line);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Failed to export metrics CSV: " + e.getMessage());
        }
    }
}



