package com.example.soccer.model;

public class ClassificationMetrics {
    private final double accuracy;
    private final double[][] confusionMatrix;
    private final double[] precision;
    private final double[] recall;
    private final double[] f1Score;
    private final String[] classNames;

    public ClassificationMetrics(double accuracy, double[][] confusionMatrix,
                                 double[] precision, double[] recall, double[] f1Score,
                                 String[] classNames) {
        this.accuracy = accuracy;
        this.confusionMatrix = confusionMatrix;
        this.precision = precision;
        this.recall = recall;
        this.f1Score = f1Score;
        this.classNames = classNames;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public double[][] getConfusionMatrix() {
        return confusionMatrix;
    }

    public double[] getPrecision() {
        return precision;
    }

    public double[] getRecall() {
        return recall;
    }

    public double[] getF1Score() {
        return f1Score;
    }

    public void print() {
        System.out.println("\n=== Classification Metrics ===");
        System.out.printf("Overall Accuracy: %.4f (%.2f%%)%n", accuracy, accuracy * 100);
        System.out.println("\nConfusion Matrix:");
        System.out.print("        ");
        for (String className : classNames) {
            System.out.printf("%8s", className);
        }
        System.out.println();
        for (int i = 0; i < confusionMatrix.length; i++) {
            System.out.printf("%-7s ", "True " + classNames[i]);
            for (int j = 0; j < confusionMatrix[i].length; j++) {
                System.out.printf("%8.0f", confusionMatrix[i][j]);
            }
            System.out.println();
        }
        System.out.println("\nPer-Class Metrics:");
        System.out.printf("%-12s %10s %10s %10s%n", "Class", "Precision", "Recall", "F1-Score");
        for (int i = 0; i < classNames.length; i++) {
            System.out.printf("%-12s %10.4f %10.4f %10.4f%n",
                    classNames[i], precision[i], recall[i], f1Score[i]);
        }
        System.out.println();
    }

    public static ClassificationMetrics compute(int[] yTrue, int[] yPred, int numClasses) {
        String[] classNames = {"Home Win", "Draw", "Away Win"};
        double[][] confusionMatrix = new double[numClasses][numClasses];
        int[] truePositives = new int[numClasses];
        int[] falsePositives = new int[numClasses];
        int[] falseNegatives = new int[numClasses];

        // Build confusion matrix
        for (int i = 0; i < yTrue.length; i++) {
            confusionMatrix[yTrue[i]][yPred[i]]++;
            if (yTrue[i] == yPred[i]) {
                truePositives[yTrue[i]]++;
            } else {
                falseNegatives[yTrue[i]]++;
                falsePositives[yPred[i]]++;
            }
        }

        // Calculate accuracy
        int correct = 0;
        for (int i = 0; i < yTrue.length; i++) {
            if (yTrue[i] == yPred[i]) correct++;
        }
        double accuracy = (double) correct / yTrue.length;

        // Calculate precision, recall, F1
        double[] precision = new double[numClasses];
        double[] recall = new double[numClasses];
        double[] f1Score = new double[numClasses];

        for (int i = 0; i < numClasses; i++) {
            double tp = truePositives[i];
            double fp = falsePositives[i];
            double fn = falseNegatives[i];

            precision[i] = (tp + fp > 0) ? tp / (tp + fp) : 0.0;
            recall[i] = (tp + fn > 0) ? tp / (tp + fn) : 0.0;
            f1Score[i] = (precision[i] + recall[i] > 0) ?
                    2 * precision[i] * recall[i] / (precision[i] + recall[i]) : 0.0;
        }

        return new ClassificationMetrics(accuracy, confusionMatrix, precision, recall, f1Score, classNames);
    }
}



