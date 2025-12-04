package com.example.soccer.weka;

import weka.classifiers.trees.RandomForest;
import weka.core.Debug;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.classifiers.Evaluation;

import java.util.Random;

public final class WekaRunner {

    private WekaRunner() {
    }

    public static void runRandomForestOnCombined(String arffPath) throws Exception {
        System.out.println();
        System.out.println("========================================");
        System.out.println("WEKA RANDOM FOREST (Combined, 10-fold CV)");
        System.out.println("========================================");

        ConverterUtils.DataSource source = new ConverterUtils.DataSource(arffPath);
        Instances data = source.getDataSet();
        if (data.classIndex() == -1) {
            data.setClassIndex(data.numAttributes() - 1);
        }

        RandomForest rf = new RandomForest();
        // Use default parameters; can be tuned via options string if desired.

        Evaluation eval = new Evaluation(data);
        eval.crossValidateModel(rf, data, 10, new Debug.Random(1));

        System.out.printf("Correctly Classified Instances: %.2f%%%n", eval.pctCorrect());
        System.out.println(eval.toSummaryString("\n=== Summary ===\n", false));
        System.out.println(eval.toClassDetailsString("\n=== Class Details ===\n"));
        System.out.println(eval.toMatrixString("\n=== Confusion Matrix ===\n"));
    }
}


