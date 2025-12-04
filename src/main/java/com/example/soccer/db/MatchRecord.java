package com.example.soccer.db;

public class MatchRecord {
    // Label: 0 = home win, 1 = draw, 2 = away win
    private final int outcome;

    // Betting odds features
    private final double homeOdds;
    private final double drawOdds;
    private final double awayOdds;

    // Team attributes - Home team
    private final double homeTeamOverall;
    private final double homeAggression;
    private final double homePassing;
    private final double homeShooting;
    private final double homeDefence;
    private final double homeBuildUpSpeed;
    private final double homeRecentGoalsFor;
    private final double homeRecentGoalsAgainst;
    private final double homeRecentGoalDiff;
    private final double homeRecentWinRate;

    // Team attributes - Away team
    private final double awayTeamOverall;
    private final double awayAggression;
    private final double awayPassing;
    private final double awayShooting;
    private final double awayDefence;
    private final double awayBuildUpSpeed;
    private final double awayRecentGoalsFor;
    private final double awayRecentGoalsAgainst;
    private final double awayRecentGoalDiff;
    private final double awayRecentWinRate;

    public MatchRecord(int outcome,
                       double homeOdds, double drawOdds, double awayOdds,
                       double homeTeamOverall, double homeAggression, double homePassing,
                       double homeShooting, double homeDefence, double homeBuildUpSpeed,
                       double homeRecentGoalsFor, double homeRecentGoalsAgainst,
                       double homeRecentGoalDiff, double homeRecentWinRate,
                       double awayTeamOverall, double awayAggression, double awayPassing,
                       double awayShooting, double awayDefence, double awayBuildUpSpeed,
                       double awayRecentGoalsFor, double awayRecentGoalsAgainst,
                       double awayRecentGoalDiff, double awayRecentWinRate) {
        this.outcome = outcome;
        this.homeOdds = homeOdds;
        this.drawOdds = drawOdds;
        this.awayOdds = awayOdds;
        this.homeTeamOverall = homeTeamOverall;
        this.homeAggression = homeAggression;
        this.homePassing = homePassing;
        this.homeShooting = homeShooting;
        this.homeDefence = homeDefence;
        this.homeBuildUpSpeed = homeBuildUpSpeed;
        this.homeRecentGoalsFor = homeRecentGoalsFor;
        this.homeRecentGoalsAgainst = homeRecentGoalsAgainst;
        this.homeRecentGoalDiff = homeRecentGoalDiff;
        this.homeRecentWinRate = homeRecentWinRate;
        this.awayTeamOverall = awayTeamOverall;
        this.awayAggression = awayAggression;
        this.awayPassing = awayPassing;
        this.awayShooting = awayShooting;
        this.awayDefence = awayDefence;
        this.awayBuildUpSpeed = awayBuildUpSpeed;
        this.awayRecentGoalsFor = awayRecentGoalsFor;
        this.awayRecentGoalsAgainst = awayRecentGoalsAgainst;
        this.awayRecentGoalDiff = awayRecentGoalDiff;
        this.awayRecentWinRate = awayRecentWinRate;
    }

    public int getOutcome() {
        return outcome;
    }

    public double getHomeOdds() {
        return homeOdds;
    }

    public double getDrawOdds() {
        return drawOdds;
    }

    public double getAwayOdds() {
        return awayOdds;
    }

    public double getHomeTeamOverall() {
        return homeTeamOverall;
    }

    public double getHomeAggression() {
        return homeAggression;
    }

    public double getHomePassing() {
        return homePassing;
    }

    public double getHomeShooting() {
        return homeShooting;
    }

    public double getHomeDefence() {
        return homeDefence;
    }

    public double getHomeBuildUpSpeed() {
        return homeBuildUpSpeed;
    }
    public double getHomeRecentGoalsFor() {
        return homeRecentGoalsFor;
    }
    public double getHomeRecentGoalsAgainst() {
        return homeRecentGoalsAgainst;
    }
    public double getHomeRecentGoalDiff() {
        return homeRecentGoalDiff;
    }
    public double getHomeRecentWinRate() {
        return homeRecentWinRate;
    }

    public double getAwayTeamOverall() {
        return awayTeamOverall;
    }

    public double getAwayAggression() {
        return awayAggression;
    }

    public double getAwayPassing() {
        return awayPassing;
    }

    public double getAwayShooting() {
        return awayShooting;
    }

    public double getAwayDefence() {
        return awayDefence;
    }

    public double getAwayBuildUpSpeed() {
        return awayBuildUpSpeed;
    }
    public double getAwayRecentGoalsFor() {
        return awayRecentGoalsFor;
    }
    public double getAwayRecentGoalsAgainst() {
        return awayRecentGoalsAgainst;
    }
    public double getAwayRecentGoalDiff() {
        return awayRecentGoalDiff;
    }
    public double getAwayRecentWinRate() {
        return awayRecentWinRate;
    }
}


