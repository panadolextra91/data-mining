package com.example.soccer.model;

import smile.classification.LogisticRegression;
import smile.classification.RandomForest;
import smile.classification.LDA;
import smile.data.DataFrame;

public class TrainedModel {
    private final String featureSetName;
    private final String modelType;
    private final Object model;
    private final ClassificationMetrics metrics;

    public TrainedModel(String featureSetName, String modelType, Object model, ClassificationMetrics metrics) {
        this.featureSetName = featureSetName;
        this.modelType = modelType;
        this.model = model;
        this.metrics = metrics;
    }

    public int predict(double[] features) {
        if (model instanceof LogisticRegression) {
            return ((LogisticRegression) model).predict(features);
        } else if (model instanceof RandomForest) {
            DataFrame df = createDataFrameForPrediction(features);
            return ((RandomForest) model).predict(df.get(0));
        } else if (model instanceof LDA) {
            return ((LDA) model).predict(features);
        }
        throw new UnsupportedOperationException("Model type not supported for prediction: " + modelType);
    }

    private DataFrame createDataFrameForPrediction(double[] features) {
        // Create a simple DataFrame with one row
        double[][] data = new double[][]{features};
        String[] columnNames = new String[features.length];
        for (int i = 0; i < features.length; i++) {
            columnNames[i] = "x" + i;
        }
        return DataFrame.of(data, columnNames);
    }

    public String getFeatureSetName() {
        return featureSetName;
    }

    public String getModelType() {
        return modelType;
    }

    public ClassificationMetrics getMetrics() {
        return metrics;
    }
}



