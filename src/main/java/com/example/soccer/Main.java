package com.example.soccer;

import com.example.soccer.db.MatchRecord;
import com.example.soccer.db.MatchRepository;
import com.example.soccer.feature.FeatureBuilder;
import com.example.soccer.feature.FeatureNormalizer;
import com.example.soccer.model.ModelTrainer;
import com.example.soccer.model.TrainedModel;
import com.example.soccer.reporting.MetricsExporter;
import com.example.soccer.weka.ArffExporter;
import com.example.soccer.weka.WekaRunner;
import com.example.soccer.sequence.SequenceMiner;

import java.sql.SQLException;
import java.util.*;

public class Main {
    private static final double DEFAULT_FORM_GOALS = 1.4;
    private static final double DEFAULT_FORM_WIN_RATE = 0.33;

    private static List<TrainedModel> trainedModels = new ArrayList<>();
    private static Map<String, FeatureNormalizer.MinMax> normalizationParams = new HashMap<>();

    public static void main(String[] args) throws SQLException {
        System.out.println("========================================");
        System.out.println("Soccer Match Outcome Prediction - Java");
        System.out.println("========================================");
        System.out.println("Using SQLite DB at: " + Config.SQLITE_DB_PATH);
        System.out.println();

        MatchRepository repo = new MatchRepository();
        List<MatchRecord> records = repo.loadMatchesWithOddsAndTeamOverall(20000);
        System.out.println("Loaded records: " + records.size());

        if (records.size() < 1000) {
            System.err.println("Not enough records loaded. Check DB path and schema.");
            return;
        }

        // Shuffle and split train/test
        Collections.shuffle(records);
        int trainSize = (int) (records.size() * 0.8);
        List<MatchRecord> train = records.subList(0, trainSize);
        List<MatchRecord> test = records.subList(trainSize, records.size());

        System.out.println("Training set: " + train.size() + " records");
        System.out.println("Test set: " + test.size() + " records");
        System.out.println();

        // Export full dataset (all records) to ARFF for Weka
        try {
            String arffPath = "target/soccer_combined.arff";
            ArffExporter.exportCombinedToArff(records, arffPath);
            System.out.println("Exported combined features to ARFF: " + arffPath);
        } catch (Exception e) {
            System.err.println("Failed to export ARFF file: " + e.getMessage());
        }

        // Build all feature sets
        Map<String, double[][]> xTrain = new HashMap<>();
        Map<String, double[][]> xTest = new HashMap<>();

        xTrain.put("Odds", FeatureBuilder.buildOddsFeatures(train));
        xTest.put("Odds", FeatureBuilder.buildOddsFeatures(test));

        xTrain.put("Team", FeatureBuilder.buildTeamFeatures(train));
        xTest.put("Team", FeatureBuilder.buildTeamFeatures(test));

        xTrain.put("Aggression", FeatureBuilder.buildAggressionFeatures(train));
        xTest.put("Aggression", FeatureBuilder.buildAggressionFeatures(test));

        xTrain.put("Passing", FeatureBuilder.buildPassingFeatures(train));
        xTest.put("Passing", FeatureBuilder.buildPassingFeatures(test));

        xTrain.put("Shooting", FeatureBuilder.buildShootingFeatures(train));
        xTest.put("Shooting", FeatureBuilder.buildShootingFeatures(test));

        xTrain.put("ComprehensiveTeam", FeatureBuilder.buildComprehensiveTeamFeatures(train));
        xTest.put("ComprehensiveTeam", FeatureBuilder.buildComprehensiveTeamFeatures(test));

        xTrain.put("Form", FeatureBuilder.buildFormFeatures(train));
        xTest.put("Form", FeatureBuilder.buildFormFeatures(test));

        xTrain.put("Combined", FeatureBuilder.buildCombinedFeatures(train));
        xTest.put("Combined", FeatureBuilder.buildCombinedFeatures(test));

        // Enhanced combined features with differences and ratios
        xTrain.put("EnhancedCombined", FeatureBuilder.buildEnhancedCombinedFeatures(train));
        xTest.put("EnhancedCombined", FeatureBuilder.buildEnhancedCombinedFeatures(test));

        int[] yTrain = FeatureBuilder.buildLabels(train);
        int[] yTest = FeatureBuilder.buildLabels(test);

        // Normalize all feature sets (important for model performance)
        System.out.println("Normalizing features...");
        for (String featureSet : xTrain.keySet()) {
            // Compute normalization parameters from training set
            FeatureNormalizer.MinMax minMax = FeatureNormalizer.computeMinMax(xTrain.get(featureSet));
            normalizationParams.put(featureSet, minMax);
            
            // Normalize training and test sets
            FeatureNormalizer.normalize(xTrain.get(featureSet));
            // For test set, we need to normalize using training set min/max
            double[][] xTestRaw = xTest.get(featureSet);
            for (int i = 0; i < xTestRaw.length; i++) {
                double[] normalized = FeatureNormalizer.normalizeSingle(
                        xTestRaw[i], minMax.getMin(), minMax.getMax());
                System.arraycopy(normalized, 0, xTestRaw[i], 0, normalized.length);
            }
        }
        System.out.println("Feature normalization complete.\n");

        // Train multiple models
        System.out.println("========================================");
        System.out.println("TRAINING MODELS");
        System.out.println("========================================");

        // Focus on best feature sets: Odds, Combined, EnhancedCombined, Form
        List<String> keyFeatureSets = Arrays.asList("Odds", "Combined", "EnhancedCombined", "Form");
        
        // Logistic Regression on key feature sets
        for (String featureSet : keyFeatureSets) {
            TrainedModel model = ModelTrainer.trainLogistic(featureSet,
                    xTrain.get(featureSet), yTrain, xTest.get(featureSet), yTest);
            if (model != null) {
                trainedModels.add(model);
                MetricsExporter.appendMetrics("target/metrics.csv", model);
            }
        }

        // Also try ComprehensiveTeam
        TrainedModel model = ModelTrainer.trainLogistic("ComprehensiveTeam",
                xTrain.get("ComprehensiveTeam"), yTrain, xTest.get("ComprehensiveTeam"), yTest);
        if (model != null) {
            trainedModels.add(model);
            MetricsExporter.appendMetrics("target/metrics.csv", model);
        }

        // Random Forest on enhanced features
        for (String featureSet : Arrays.asList("Odds", "Combined", "EnhancedCombined")) {
            TrainedModel rfModel = ModelTrainer.trainRandomForest(featureSet,
                    xTrain.get(featureSet), yTrain, xTest.get(featureSet), yTest);
            if (rfModel != null) {
                trainedModels.add(rfModel);
                MetricsExporter.appendMetrics("target/metrics.csv", rfModel);
            }
        }

        // LDA on key feature sets
        for (String featureSet : keyFeatureSets) {
            TrainedModel ldaModel = ModelTrainer.trainLDA(featureSet,
                    xTrain.get(featureSet), yTrain, xTest.get(featureSet), yTest);
            if (ldaModel != null) {
                trainedModels.add(ldaModel);
                MetricsExporter.appendMetrics("target/metrics.csv", ldaModel);
            }
        }

        // Summary comparison
        printModelComparison();

        // Weka RandomForest with 10-fold CV on combined features
        try {
            WekaRunner.runRandomForestOnCombined("target/soccer_combined.arff");
        } catch (Exception e) {
            System.err.println("Weka RandomForest failed: " + e.getMessage());
        }

        // Sequence mining: frequent patterns of W/D/L per team
        try {
            SequenceMiner.printTopPatterns(3, 10);
        } catch (Exception e) {
            System.err.println("Sequence mining failed: " + e.getMessage());
        }

        // Interactive mode
        System.out.println("\n========================================");
        System.out.println("INTERACTIVE PREDICTION MODE");
        System.out.println("========================================");
        interactiveMode();
    }

    private static void printModelComparison() {
        System.out.println("\n========================================");
        System.out.println("MODEL COMPARISON SUMMARY");
        System.out.println("========================================");
        System.out.printf("%-25s %-20s %10s%n", "Model", "Feature Set", "Accuracy");
        System.out.println("--------------------------------------------------------");
        
        trainedModels.sort((a, b) -> Double.compare(b.getMetrics().getAccuracy(), 
                a.getMetrics().getAccuracy()));
        
        for (TrainedModel model : trainedModels) {
            System.out.printf("%-25s %-20s %10.4f%n",
                    model.getModelType(),
                    model.getFeatureSetName(),
                    model.getMetrics().getAccuracy());
        }
    }

    private static void interactiveMode() {
        Scanner scanner = new Scanner(System.in);
        String[] outcomes = {"Home Win", "Draw", "Away Win"};

        System.out.println("\nEnter match details to get predictions from trained models.");
        System.out.println("Type 'quit' to exit.\n");

        while (true) {
            try {
                System.out.print("Enter home team odds (e.g., 2.5): ");
                String input = scanner.nextLine().trim();
                if (input.equalsIgnoreCase("quit")) {
                    System.out.println("Exiting interactive mode. Goodbye!");
                    break;
                }
                double homeOdds = Double.parseDouble(input);

                System.out.print("Enter draw odds (e.g., 3.0): ");
                double drawOdds = Double.parseDouble(scanner.nextLine().trim());

                System.out.print("Enter away team odds (e.g., 2.8): ");
                double awayOdds = Double.parseDouble(scanner.nextLine().trim());

                System.out.print("Enter home team overall (0-100, e.g., 70): ");
                double homeOverall = Double.parseDouble(scanner.nextLine().trim());

                System.out.print("Enter home team aggression (0-100, e.g., 60): ");
                double homeAggression = Double.parseDouble(scanner.nextLine().trim());

                System.out.print("Enter home team passing (0-100, e.g., 75): ");
                double homePassing = Double.parseDouble(scanner.nextLine().trim());

                System.out.print("Enter home team shooting (0-100, e.g., 70): ");
                double homeShooting = Double.parseDouble(scanner.nextLine().trim());

                System.out.print("Enter home team defence (0-100, e.g., 65): ");
                double homeDefence = Double.parseDouble(scanner.nextLine().trim());

                System.out.print("Enter home team build-up speed (0-100, e.g., 60): ");
                double homeBuildUpSpeed = Double.parseDouble(scanner.nextLine().trim());

                System.out.print("Enter away team overall (0-100, e.g., 68): ");
                double awayOverall = Double.parseDouble(scanner.nextLine().trim());

                System.out.print("Enter away team aggression (0-100, e.g., 55): ");
                double awayAggression = Double.parseDouble(scanner.nextLine().trim());

                System.out.print("Enter away team passing (0-100, e.g., 72): ");
                double awayPassing = Double.parseDouble(scanner.nextLine().trim());

                System.out.print("Enter away team shooting (0-100, e.g., 68): ");
                double awayShooting = Double.parseDouble(scanner.nextLine().trim());

                System.out.print("Enter away team defence (0-100, e.g., 70): ");
                double awayDefence = Double.parseDouble(scanner.nextLine().trim());

                System.out.print("Enter away team build-up speed (0-100, e.g., 58): ");
                double awayBuildUpSpeed = Double.parseDouble(scanner.nextLine().trim());

                double homeRecentGoalsFor = readDoubleWithDefault(scanner,
                        "Enter home avg goals scored in last 5 matches (default 1.4): ",
                        DEFAULT_FORM_GOALS);
                double homeRecentGoalsAgainst = readDoubleWithDefault(scanner,
                        "Enter home avg goals conceded in last 5 matches (default 1.4): ",
                        DEFAULT_FORM_GOALS);
                double homeRecentWinRate = readDoubleWithDefault(scanner,
                        "Enter home recent win rate (0-1, default 0.33): ",
                        DEFAULT_FORM_WIN_RATE);

                double awayRecentGoalsFor = readDoubleWithDefault(scanner,
                        "Enter away avg goals scored in last 5 matches (default 1.4): ",
                        DEFAULT_FORM_GOALS);
                double awayRecentGoalsAgainst = readDoubleWithDefault(scanner,
                        "Enter away avg goals conceded in last 5 matches (default 1.4): ",
                        DEFAULT_FORM_GOALS);
                double awayRecentWinRate = readDoubleWithDefault(scanner,
                        "Enter away recent win rate (0-1, default 0.33): ",
                        DEFAULT_FORM_WIN_RATE);

                double homeRecentGoalDiff = homeRecentGoalsFor - homeRecentGoalsAgainst;
                double awayRecentGoalDiff = awayRecentGoalsFor - awayRecentGoalsAgainst;

                // Create a match record for prediction
                MatchRecord match = new MatchRecord(0, // dummy outcome
                        homeOdds, drawOdds, awayOdds,
                        homeOverall, homeAggression, homePassing,
                        homeShooting, homeDefence, homeBuildUpSpeed,
                        homeRecentGoalsFor, homeRecentGoalsAgainst,
                        homeRecentGoalDiff, homeRecentWinRate,
                        awayOverall, awayAggression, awayPassing,
                        awayShooting, awayDefence, awayBuildUpSpeed,
                        awayRecentGoalsFor, awayRecentGoalsAgainst,
                        awayRecentGoalDiff, awayRecentWinRate);

                // Get predictions from all models
                System.out.println("\n========================================");
                System.out.println("PREDICTIONS");
                System.out.println("========================================");
                System.out.printf("%-25s %-20s %15s%n", "Model", "Feature Set", "Prediction");
                System.out.println("--------------------------------------------------------");

                for (TrainedModel model : trainedModels) {
                    try {
                        double[] features;
                        String featureSet = model.getFeatureSetName();
                        switch (featureSet) {
                            case "Odds":
                                features = FeatureBuilder.buildOddsFeaturesSingle(match);
                                break;
                            case "Team":
                                features = FeatureBuilder.buildTeamFeaturesSingle(match);
                                break;
                            case "Aggression":
                                features = FeatureBuilder.buildAggressionFeaturesSingle(match);
                                break;
                            case "Passing":
                                features = FeatureBuilder.buildPassingFeaturesSingle(match);
                                break;
                            case "Shooting":
                                features = FeatureBuilder.buildShootingFeaturesSingle(match);
                                break;
                            case "ComprehensiveTeam":
                                features = FeatureBuilder.buildComprehensiveTeamFeaturesSingle(match);
                                break;
                            case "Combined":
                                features = FeatureBuilder.buildCombinedFeaturesSingle(match);
                                break;
                            case "Form":
                                features = FeatureBuilder.buildFormFeaturesSingle(match);
                                break;
                            case "EnhancedCombined":
                                features = FeatureBuilder.buildEnhancedCombinedFeaturesSingle(match);
                                break;
                            default:
                                continue;
                        }
                        // Normalize features using training set parameters
                        if (normalizationParams.containsKey(featureSet)) {
                            FeatureNormalizer.MinMax minMax = normalizationParams.get(featureSet);
                            features = FeatureNormalizer.normalizeSingle(features, minMax.getMin(), minMax.getMax());
                        }
                        int prediction = model.predict(features);
                        System.out.printf("%-25s %-20s %15s%n",
                                model.getModelType(),
                                model.getFeatureSetName(),
                                outcomes[prediction]);
                    } catch (Exception e) {
                        // Skip models that fail
                    }
                }
                System.out.println();

            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter numeric values.\n");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage() + "\n");
            }
        }
        scanner.close();
    }

    private static double readDoubleWithDefault(Scanner scanner, String prompt, double defaultValue) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                return defaultValue;
            }
            try {
                return Double.parseDouble(line);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a numeric value.");
            }
        }
    }
}



