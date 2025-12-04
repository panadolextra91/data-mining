package com.example.soccer.model;

import smile.classification.LogisticRegression;
import smile.classification.RandomForest;
import smile.classification.LDA;
import smile.data.DataFrame;
import smile.data.formula.Formula;

public final class ModelTrainer {

    private ModelTrainer() {
    }

    public static TrainedModel trainLogistic(String name, double[][] xTrain, int[] yTrain,
                                             double[][] xTest, int[] yTest) {
        LogisticRegression model = LogisticRegression.fit(xTrain, yTrain);
        int[] yPred = predict(model, xTest);
        ClassificationMetrics metrics = ClassificationMetrics.compute(yTest, yPred, 3);
        System.out.println("\n=== Logistic Regression: " + name + " ===");
        metrics.print();
        return new TrainedModel(name, "LogisticRegression", model, metrics);
    }

    public static TrainedModel trainRandomForest(String name, double[][] xTrain, int[] yTrain,
                                                  double[][] xTest, int[] yTest) {
        try {
            DataFrame trainDf = toDataFrame(xTrain, yTrain);
            DataFrame testDf = toDataFrame(xTest, new int[xTest.length]);
            
            Formula formula = Formula.lhs("label");
            RandomForest model = RandomForest.fit(formula, trainDf);
            
            int[] yPred = new int[xTest.length];
            for (int i = 0; i < xTest.length; i++) {
                yPred[i] = model.predict(testDf.get(i));
            }
            
            ClassificationMetrics metrics = ClassificationMetrics.compute(yTest, yPred, 3);
            System.out.println("\n=== Random Forest: " + name + " ===");
            metrics.print();
            return new TrainedModel(name, "RandomForest", model, metrics);
        } catch (Exception e) {
            System.err.println("Random Forest training failed: " + e.getMessage());
            return null;
        }
    }

    public static TrainedModel trainLDA(String name, double[][] xTrain, int[] yTrain,
                                        double[][] xTest, int[] yTest) {
        try {
            LDA model = LDA.fit(xTrain, yTrain);
            int[] yPred = predict(model, xTest);
            ClassificationMetrics metrics = ClassificationMetrics.compute(yTest, yPred, 3);
            System.out.println("\n=== LDA: " + name + " ===");
            metrics.print();
            return new TrainedModel(name, "LDA", model, metrics);
        } catch (Exception e) {
            System.err.println("LDA training failed: " + e.getMessage());
            return null;
        }
    }

    private static int[] predict(smile.classification.Classifier<double[]> model, double[][] x) {
        int[] yPred = new int[x.length];
        for (int i = 0; i < x.length; i++) {
            yPred[i] = model.predict(x[i]);
        }
        return yPred;
    }

    private static DataFrame toDataFrame(double[][] x, int[] y) {
        int n = x.length;
        int d = x[0].length;
        
        // Create column names
        String[] columnNames = new String[y != null && y.length > 0 ? d + 1 : d];
        for (int i = 0; i < d; i++) {
            columnNames[i] = "x" + i;
        }
        if (y != null && y.length > 0) {
            columnNames[d] = "label";
        }
        
        // Create data array with label column if needed
        if (y != null && y.length > 0) {
            double[][] dataWithLabel = new double[n][d + 1];
            for (int i = 0; i < n; i++) {
                System.arraycopy(x[i], 0, dataWithLabel[i], 0, d);
                dataWithLabel[i][d] = y[i];
            }
            return DataFrame.of(dataWithLabel, columnNames);
        } else {
            return DataFrame.of(x, columnNames);
        }
    }
}



