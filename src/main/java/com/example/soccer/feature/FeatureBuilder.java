package com.example.soccer.feature;

import com.example.soccer.db.MatchRecord;

import java.util.List;

public final class FeatureBuilder {

    private static final int ENHANCED_FEATURE_COUNT = 40;

    private FeatureBuilder() {
    }

    /**
     * Features using only betting odds (H, D, A).
     */
    public static double[][] buildOddsFeatures(List<MatchRecord> records) {
        double[][] x = new double[records.size()][3];
        for (int i = 0; i < records.size(); i++) {
            MatchRecord r = records.get(i);
            x[i][0] = r.getHomeOdds();
            x[i][1] = r.getDrawOdds();
            x[i][2] = r.getAwayOdds();
        }
        return x;
    }

    /**
     * Features using basic team attributes (home/away overall proxies).
     */
    public static double[][] buildTeamFeatures(List<MatchRecord> records) {
        double[][] x = new double[records.size()][2];
        for (int i = 0; i < records.size(); i++) {
            MatchRecord r = records.get(i);
            x[i][0] = r.getHomeTeamOverall();
            x[i][1] = r.getAwayTeamOverall();
        }
        return x;
    }

    /**
     * Features using only aggression attributes.
     */
    public static double[][] buildAggressionFeatures(List<MatchRecord> records) {
        double[][] x = new double[records.size()][2];
        for (int i = 0; i < records.size(); i++) {
            MatchRecord r = records.get(i);
            x[i][0] = r.getHomeAggression();
            x[i][1] = r.getAwayAggression();
        }
        return x;
    }

    /**
     * Features using only passing attributes.
     */
    public static double[][] buildPassingFeatures(List<MatchRecord> records) {
        double[][] x = new double[records.size()][2];
        for (int i = 0; i < records.size(); i++) {
            MatchRecord r = records.get(i);
            x[i][0] = r.getHomePassing();
            x[i][1] = r.getAwayPassing();
        }
        return x;
    }

    /**
     * Features using only shooting attributes.
     */
    public static double[][] buildShootingFeatures(List<MatchRecord> records) {
        double[][] x = new double[records.size()][2];
        for (int i = 0; i < records.size(); i++) {
            MatchRecord r = records.get(i);
            x[i][0] = r.getHomeShooting();
            x[i][1] = r.getAwayShooting();
        }
        return x;
    }

    /**
     * Comprehensive features: all team attributes (12 features).
     */
    public static double[][] buildComprehensiveTeamFeatures(List<MatchRecord> records) {
        double[][] x = new double[records.size()][12];
        for (int i = 0; i < records.size(); i++) {
            MatchRecord r = records.get(i);
            x[i][0] = r.getHomeTeamOverall();
            x[i][1] = r.getHomeAggression();
            x[i][2] = r.getHomePassing();
            x[i][3] = r.getHomeShooting();
            x[i][4] = r.getHomeDefence();
            x[i][5] = r.getHomeBuildUpSpeed();
            x[i][6] = r.getAwayTeamOverall();
            x[i][7] = r.getAwayAggression();
            x[i][8] = r.getAwayPassing();
            x[i][9] = r.getAwayShooting();
            x[i][10] = r.getAwayDefence();
            x[i][11] = r.getAwayBuildUpSpeed();
        }
        return x;
    }

    /**
     * Recent form statistics (avg goals for/against, goal diff, win rate) for both teams.
     */
    public static double[][] buildFormFeatures(List<MatchRecord> records) {
        double[][] x = new double[records.size()][8];
        for (int i = 0; i < records.size(); i++) {
            MatchRecord r = records.get(i);
            x[i][0] = r.getHomeRecentGoalsFor();
            x[i][1] = r.getHomeRecentGoalsAgainst();
            x[i][2] = r.getHomeRecentGoalDiff();
            x[i][3] = r.getHomeRecentWinRate();
            x[i][4] = r.getAwayRecentGoalsFor();
            x[i][5] = r.getAwayRecentGoalsAgainst();
            x[i][6] = r.getAwayRecentGoalDiff();
            x[i][7] = r.getAwayRecentWinRate();
        }
        return x;
    }

    /**
     * Combined features: odds + team attributes + form stats (23 features).
     */
    public static double[][] buildCombinedFeatures(List<MatchRecord> records) {
        double[][] x = new double[records.size()][23];
        for (int i = 0; i < records.size(); i++) {
            MatchRecord r = records.get(i);
            // Odds (3)
            x[i][0] = r.getHomeOdds();
            x[i][1] = r.getDrawOdds();
            x[i][2] = r.getAwayOdds();
            // Home team attributes (6)
            x[i][3] = r.getHomeTeamOverall();
            x[i][4] = r.getHomeAggression();
            x[i][5] = r.getHomePassing();
            x[i][6] = r.getHomeShooting();
            x[i][7] = r.getHomeDefence();
            x[i][8] = r.getHomeBuildUpSpeed();
            // Away team attributes (6)
            x[i][9] = r.getAwayTeamOverall();
            x[i][10] = r.getAwayAggression();
            x[i][11] = r.getAwayPassing();
            x[i][12] = r.getAwayShooting();
            x[i][13] = r.getAwayDefence();
            x[i][14] = r.getAwayBuildUpSpeed();
            // Form stats (8)
            x[i][15] = r.getHomeRecentGoalsFor();
            x[i][16] = r.getHomeRecentGoalsAgainst();
            x[i][17] = r.getHomeRecentGoalDiff();
            x[i][18] = r.getHomeRecentWinRate();
            x[i][19] = r.getAwayRecentGoalsFor();
            x[i][20] = r.getAwayRecentGoalsAgainst();
            x[i][21] = r.getAwayRecentGoalDiff();
            x[i][22] = r.getAwayRecentWinRate();
        }
        return x;
    }

    /**
     * Enhanced combined features with odds, team attributes, form stats, differences, and ratios.
     */
    public static double[][] buildEnhancedCombinedFeatures(List<MatchRecord> records) {
        double[][] x = new double[records.size()][ENHANCED_FEATURE_COUNT];
        for (int i = 0; i < records.size(); i++) {
            MatchRecord r = records.get(i);
            int idx = 0;

            // Raw odds
            x[i][idx++] = r.getHomeOdds();
            x[i][idx++] = r.getDrawOdds();
            x[i][idx++] = r.getAwayOdds();

            // Normalized odds (inverse probabilities)
            double homeProb = 1.0 / r.getHomeOdds();
            double drawProb = 1.0 / r.getDrawOdds();
            double awayProb = 1.0 / r.getAwayOdds();
            double totalProb = homeProb + drawProb + awayProb;
            x[i][idx++] = homeProb / totalProb;
            x[i][idx++] = drawProb / totalProb;
            x[i][idx++] = awayProb / totalProb;

            // Odds ratios
            x[i][idx++] = r.getHomeOdds() / r.getAwayOdds();
            x[i][idx++] = r.getDrawOdds() / Math.min(r.getHomeOdds(), r.getAwayOdds());

            // Home team attributes (6)
            x[i][idx++] = r.getHomeTeamOverall();
            x[i][idx++] = r.getHomeAggression();
            x[i][idx++] = r.getHomePassing();
            x[i][idx++] = r.getHomeShooting();
            x[i][idx++] = r.getHomeDefence();
            x[i][idx++] = r.getHomeBuildUpSpeed();

            // Away team attributes (6)
            x[i][idx++] = r.getAwayTeamOverall();
            x[i][idx++] = r.getAwayAggression();
            x[i][idx++] = r.getAwayPassing();
            x[i][idx++] = r.getAwayShooting();
            x[i][idx++] = r.getAwayDefence();
            x[i][idx++] = r.getAwayBuildUpSpeed();

            // Form stats (8)
            x[i][idx++] = r.getHomeRecentGoalsFor();
            x[i][idx++] = r.getHomeRecentGoalsAgainst();
            x[i][idx++] = r.getHomeRecentGoalDiff();
            x[i][idx++] = r.getHomeRecentWinRate();
            x[i][idx++] = r.getAwayRecentGoalsFor();
            x[i][idx++] = r.getAwayRecentGoalsAgainst();
            x[i][idx++] = r.getAwayRecentGoalDiff();
            x[i][idx++] = r.getAwayRecentWinRate();

            // Team attribute differences (6)
            x[i][idx++] = r.getHomeTeamOverall() - r.getAwayTeamOverall();
            x[i][idx++] = r.getHomeAggression() - r.getAwayAggression();
            x[i][idx++] = r.getHomePassing() - r.getAwayPassing();
            x[i][idx++] = r.getHomeShooting() - r.getAwayShooting();
            x[i][idx++] = r.getHomeDefence() - r.getAwayDefence();
            x[i][idx++] = r.getHomeBuildUpSpeed() - r.getAwayBuildUpSpeed();

            // Form differences (4)
            x[i][idx++] = r.getHomeRecentGoalsFor() - r.getAwayRecentGoalsFor();
            x[i][idx++] = r.getHomeRecentGoalsAgainst() - r.getAwayRecentGoalsAgainst();
            x[i][idx++] = r.getHomeRecentGoalDiff() - r.getAwayRecentGoalDiff();
            x[i][idx++] = r.getHomeRecentWinRate() - r.getAwayRecentWinRate();

            // Team attribute ratios (avoid division by zero) - 2 ratios
            double awayOverall = r.getAwayTeamOverall() > 0.1 ? r.getAwayTeamOverall() : 0.1;
            double awayPassing = r.getAwayPassing() > 0.1 ? r.getAwayPassing() : 0.1;
            x[i][idx++] = r.getHomeTeamOverall() / awayOverall;
            x[i][idx++] = r.getHomePassing() / awayPassing;
        }
        return x;
    }

    public static int[] buildLabels(List<MatchRecord> records) {
        int[] y = new int[records.size()];
        for (int i = 0; i < records.size(); i++) {
            y[i] = records.get(i).getOutcome();
        }
        return y;
    }

    /**
     * Build features from a single match record (for prediction).
     */
    public static double[] buildOddsFeaturesSingle(MatchRecord record) {
        return new double[]{record.getHomeOdds(), record.getDrawOdds(), record.getAwayOdds()};
    }

    public static double[] buildTeamFeaturesSingle(MatchRecord record) {
        return new double[]{record.getHomeTeamOverall(), record.getAwayTeamOverall()};
    }

    public static double[] buildAggressionFeaturesSingle(MatchRecord record) {
        return new double[]{record.getHomeAggression(), record.getAwayAggression()};
    }

    public static double[] buildPassingFeaturesSingle(MatchRecord record) {
        return new double[]{record.getHomePassing(), record.getAwayPassing()};
    }

    public static double[] buildShootingFeaturesSingle(MatchRecord record) {
        return new double[]{record.getHomeShooting(), record.getAwayShooting()};
    }

    public static double[] buildComprehensiveTeamFeaturesSingle(MatchRecord record) {
        return new double[]{
                record.getHomeTeamOverall(), record.getHomeAggression(), record.getHomePassing(),
                record.getHomeShooting(), record.getHomeDefence(), record.getHomeBuildUpSpeed(),
                record.getAwayTeamOverall(), record.getAwayAggression(), record.getAwayPassing(),
                record.getAwayShooting(), record.getAwayDefence(), record.getAwayBuildUpSpeed()
        };
    }

    public static double[] buildFormFeaturesSingle(MatchRecord record) {
        return new double[]{
                record.getHomeRecentGoalsFor(),
                record.getHomeRecentGoalsAgainst(),
                record.getHomeRecentGoalDiff(),
                record.getHomeRecentWinRate(),
                record.getAwayRecentGoalsFor(),
                record.getAwayRecentGoalsAgainst(),
                record.getAwayRecentGoalDiff(),
                record.getAwayRecentWinRate()
        };
    }

    public static double[] buildCombinedFeaturesSingle(MatchRecord record) {
        double[] features = new double[23];
        features[0] = record.getHomeOdds();
        features[1] = record.getDrawOdds();
        features[2] = record.getAwayOdds();
        features[3] = record.getHomeTeamOverall();
        features[4] = record.getHomeAggression();
        features[5] = record.getHomePassing();
        features[6] = record.getHomeShooting();
        features[7] = record.getHomeDefence();
        features[8] = record.getHomeBuildUpSpeed();
        features[9] = record.getAwayTeamOverall();
        features[10] = record.getAwayAggression();
        features[11] = record.getAwayPassing();
        features[12] = record.getAwayShooting();
        features[13] = record.getAwayDefence();
        features[14] = record.getAwayBuildUpSpeed();
        features[15] = record.getHomeRecentGoalsFor();
        features[16] = record.getHomeRecentGoalsAgainst();
        features[17] = record.getHomeRecentGoalDiff();
        features[18] = record.getHomeRecentWinRate();
        features[19] = record.getAwayRecentGoalsFor();
        features[20] = record.getAwayRecentGoalsAgainst();
        features[21] = record.getAwayRecentGoalDiff();
        features[22] = record.getAwayRecentWinRate();
        return features;
    }

    public static double[] buildEnhancedCombinedFeaturesSingle(MatchRecord record) {
        double[] features = new double[ENHANCED_FEATURE_COUNT];
        int idx = 0;

        // Raw odds
        features[idx++] = record.getHomeOdds();
        features[idx++] = record.getDrawOdds();
        features[idx++] = record.getAwayOdds();

        // Normalized odds (inverse probabilities)
        double homeProb = 1.0 / record.getHomeOdds();
        double drawProb = 1.0 / record.getDrawOdds();
        double awayProb = 1.0 / record.getAwayOdds();
        double totalProb = homeProb + drawProb + awayProb;
        features[idx++] = homeProb / totalProb;
        features[idx++] = drawProb / totalProb;
        features[idx++] = awayProb / totalProb;

        // Odds ratios
        features[idx++] = record.getHomeOdds() / record.getAwayOdds();
        features[idx++] = record.getDrawOdds() / Math.min(record.getHomeOdds(), record.getAwayOdds());

        // Home team attributes (6)
        features[idx++] = record.getHomeTeamOverall();
        features[idx++] = record.getHomeAggression();
        features[idx++] = record.getHomePassing();
        features[idx++] = record.getHomeShooting();
        features[idx++] = record.getHomeDefence();
        features[idx++] = record.getHomeBuildUpSpeed();
        
        // Away team attributes (6)
        features[idx++] = record.getAwayTeamOverall();
        features[idx++] = record.getAwayAggression();
        features[idx++] = record.getAwayPassing();
        features[idx++] = record.getAwayShooting();
        features[idx++] = record.getAwayDefence();
        features[idx++] = record.getAwayBuildUpSpeed();

        // Form stats (8)
        features[idx++] = record.getHomeRecentGoalsFor();
        features[idx++] = record.getHomeRecentGoalsAgainst();
        features[idx++] = record.getHomeRecentGoalDiff();
        features[idx++] = record.getHomeRecentWinRate();
        features[idx++] = record.getAwayRecentGoalsFor();
        features[idx++] = record.getAwayRecentGoalsAgainst();
        features[idx++] = record.getAwayRecentGoalDiff();
        features[idx++] = record.getAwayRecentWinRate();

        // Team attribute differences (6)
        features[idx++] = record.getHomeTeamOverall() - record.getAwayTeamOverall();
        features[idx++] = record.getHomeAggression() - record.getAwayAggression();
        features[idx++] = record.getHomePassing() - record.getAwayPassing();
        features[idx++] = record.getHomeShooting() - record.getAwayShooting();
        features[idx++] = record.getHomeDefence() - record.getAwayDefence();
        features[idx++] = record.getHomeBuildUpSpeed() - record.getAwayBuildUpSpeed();

        // Form differences (4)
        features[idx++] = record.getHomeRecentGoalsFor() - record.getAwayRecentGoalsFor();
        features[idx++] = record.getHomeRecentGoalsAgainst() - record.getAwayRecentGoalsAgainst();
        features[idx++] = record.getHomeRecentGoalDiff() - record.getAwayRecentGoalDiff();
        features[idx++] = record.getHomeRecentWinRate() - record.getAwayRecentWinRate();

        // Team attribute ratios (2 ratios)
        double awayOverall = record.getAwayTeamOverall() > 0.1 ? record.getAwayTeamOverall() : 0.1;
        double awayPassing = record.getAwayPassing() > 0.1 ? record.getAwayPassing() : 0.1;
        features[idx++] = record.getHomeTeamOverall() / awayOverall;
        features[idx++] = record.getHomePassing() / awayPassing;

        return features;
    }
}


