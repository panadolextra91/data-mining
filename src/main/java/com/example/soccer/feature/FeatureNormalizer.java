package com.example.soccer.feature;

public class FeatureNormalizer {
    
    /**
     * Normalize features using min-max scaling to [0, 1] range.
     */
    public static void normalize(double[][] features) {
        if (features.length == 0) return;
        
        int numFeatures = features[0].length;
        double[] min = new double[numFeatures];
        double[] max = new double[numFeatures];
        
        // Initialize with first row
        for (int j = 0; j < numFeatures; j++) {
            min[j] = features[0][j];
            max[j] = features[0][j];
        }
        
        // Find min and max for each feature
        for (double[] row : features) {
            for (int j = 0; j < numFeatures; j++) {
                if (row[j] < min[j]) min[j] = row[j];
                if (row[j] > max[j]) max[j] = row[j];
            }
        }
        
        // Normalize
        for (double[] row : features) {
            for (int j = 0; j < numFeatures; j++) {
                double range = max[j] - min[j];
                if (range > 0.0001) {
                    row[j] = (row[j] - min[j]) / range;
                } else {
                    row[j] = 0.5; // Default to middle if constant feature
                }
            }
        }
    }
    
    /**
     * Normalize a single feature vector using pre-computed min/max.
     */
    public static double[] normalizeSingle(double[] features, double[] min, double[] max) {
        double[] normalized = new double[features.length];
        for (int j = 0; j < features.length; j++) {
            double range = max[j] - min[j];
            if (range > 0.0001) {
                normalized[j] = (features[j] - min[j]) / range;
            } else {
                normalized[j] = 0.5;
            }
        }
        return normalized;
    }
    
    /**
     * Compute min and max for normalization.
     */
    public static MinMax computeMinMax(double[][] features) {
        if (features.length == 0) {
            return new MinMax(new double[0], new double[0]);
        }
        
        int numFeatures = features[0].length;
        double[] min = new double[numFeatures];
        double[] max = new double[numFeatures];
        
        // Initialize
        for (int j = 0; j < numFeatures; j++) {
            min[j] = Double.MAX_VALUE;
            max[j] = Double.MIN_VALUE;
        }
        
        // Find min and max
        for (double[] row : features) {
            for (int j = 0; j < numFeatures; j++) {
                if (row[j] < min[j]) min[j] = row[j];
                if (row[j] > max[j]) max[j] = row[j];
            }
        }
        
        return new MinMax(min, max);
    }
    
    public static class MinMax {
        private final double[] min;
        private final double[] max;
        
        public MinMax(double[] min, double[] max) {
            this.min = min;
            this.max = max;
        }
        
        public double[] getMin() {
            return min;
        }
        
        public double[] getMax() {
            return max;
        }
    }
}



