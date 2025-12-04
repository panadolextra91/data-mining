package com.example.soccer.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads data from the Kaggle European Soccer database.
 *
 * Schema reference (simplified):
 * - match table: contains goals, team IDs, betting odds (e.g. B365H, B365D, B365A)
 * - team_attributes table: contains aggregated team stats (buildUpPlaySpeed, aggression, passing, etc.)
 */
public class MatchRepository {

    private static final double DEFAULT_GOALS_FOR = 1.4;
    private static final double DEFAULT_GOALS_AGAINST = 1.4;
    private static final double DEFAULT_WIN_RATE = 0.33;
    private static final int FORM_WINDOW = 5;

    /**
     * Load a sample of matches with:
     * - outcome label (home/draw/away)
     * - Bet365 odds (H/D/A)
     * - expanded team attributes (overall, aggression, passing, shooting, defence, buildUpSpeed)
     * - recent form features (last N matches goals/win rate)
     */
    public List<MatchRecord> loadMatchesWithOddsAndTeamOverall(int limit) throws SQLException {
        String sql = """
                SELECT
                    m.home_team_api_id,
                    m.away_team_api_id,
                    m.home_team_goal,
                    m.away_team_goal,
                    m.B365H,
                    m.B365D,
                    m.B365A,
                    -- Home team attributes (using most recent available)
                    (th.buildUpPlaySpeed + th.chanceCreationPassing + th.defencePressure) AS home_overall,
                    COALESCE(th.defenceAggression, 50.0) AS home_aggression,
                    COALESCE(th.chanceCreationPassing, 50.0) AS home_passing,
                    COALESCE(th.chanceCreationShooting, 50.0) AS home_shooting,
                    COALESCE(th.defencePressure, 50.0) AS home_defence,
                    COALESCE(th.buildUpPlaySpeed, 50.0) AS home_buildUpSpeed,
                    -- Away team attributes (using most recent available)
                    (ta.buildUpPlaySpeed + ta.chanceCreationPassing + ta.defencePressure) AS away_overall,
                    COALESCE(ta.defenceAggression, 50.0) AS away_aggression,
                    COALESCE(ta.chanceCreationPassing, 50.0) AS away_passing,
                    COALESCE(ta.chanceCreationShooting, 50.0) AS away_shooting,
                    COALESCE(ta.defencePressure, 50.0) AS away_defence,
                    COALESCE(ta.buildUpPlaySpeed, 50.0) AS away_buildUpSpeed
                FROM Match AS m
                JOIN Team_Attributes th ON th.team_api_id = m.home_team_api_id
                JOIN Team_Attributes ta ON ta.team_api_id = m.away_team_api_id
                WHERE m.B365H IS NOT NULL
                  AND m.B365D IS NOT NULL
                  AND m.B365A IS NOT NULL
                  AND th.buildUpPlaySpeed IS NOT NULL
                  AND ta.buildUpPlaySpeed IS NOT NULL
                ORDER BY m.date
                LIMIT ?
                """;

        List<MatchRecord> records = new ArrayList<>();
        Map<Integer, TeamFormWindow> formStats = new HashMap<>();

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int homeTeamId = rs.getInt(1);
                    int awayTeamId = rs.getInt(2);
                    int homeGoals = rs.getInt(3);
                    int awayGoals = rs.getInt(4);
                    double homeOdds = rs.getDouble(5);
                    double drawOdds = rs.getDouble(6);
                    double awayOdds = rs.getDouble(7);

                    // Home team attributes
                    double homeOverall = rs.getDouble(8);
                    double homeAggression = rs.getDouble(9);
                    double homePassing = rs.getDouble(10);
                    double homeShooting = rs.getDouble(11);
                    double homeDefence = rs.getDouble(12);
                    double homeBuildUpSpeed = rs.getDouble(13);

                    // Away team attributes
                    double awayOverall = rs.getDouble(14);
                    double awayAggression = rs.getDouble(15);
                    double awayPassing = rs.getDouble(16);
                    double awayShooting = rs.getDouble(17);
                    double awayDefence = rs.getDouble(18);
                    double awayBuildUpSpeed = rs.getDouble(19);

                    TeamFormWindow homeForm = formStats.computeIfAbsent(
                            homeTeamId, id -> new TeamFormWindow(FORM_WINDOW));
                    TeamFormWindow awayForm = formStats.computeIfAbsent(
                            awayTeamId, id -> new TeamFormWindow(FORM_WINDOW));

                    int outcome;
                    if (homeGoals > awayGoals) {
                        outcome = 0; // home win
                    } else if (homeGoals == awayGoals) {
                        outcome = 1; // draw
                    } else {
                        outcome = 2; // away win
                    }

                    records.add(new MatchRecord(
                            outcome,
                            homeOdds, drawOdds, awayOdds,
                            homeOverall, homeAggression, homePassing,
                            homeShooting, homeDefence, homeBuildUpSpeed,
                            homeForm.getAvgGoalsFor(), homeForm.getAvgGoalsAgainst(),
                            homeForm.getAvgGoalDiff(), homeForm.getWinRate(),
                            awayOverall, awayAggression, awayPassing,
                            awayShooting, awayDefence, awayBuildUpSpeed,
                            awayForm.getAvgGoalsFor(), awayForm.getAvgGoalsAgainst(),
                            awayForm.getAvgGoalDiff(), awayForm.getWinRate()
                    ));

                    // Update form stats with current match result
                    homeForm.addResult(homeGoals, awayGoals);
                    awayForm.addResult(awayGoals, homeGoals);
                }
            }
        }

        return records;
    }

    private static final class TeamFormWindow {
        private final int windowSize;
        private final ArrayDeque<MatchResult> history = new ArrayDeque<>();
        private double sumGoalsFor = 0.0;
        private double sumGoalsAgainst = 0.0;
        private double sumWinScore = 0.0;

        private TeamFormWindow(int windowSize) {
            this.windowSize = windowSize;
        }

        void addResult(int goalsFor, int goalsAgainst) {
            MatchResult result = new MatchResult(goalsFor, goalsAgainst);
            history.addLast(result);
            sumGoalsFor += goalsFor;
            sumGoalsAgainst += goalsAgainst;
            sumWinScore += result.winScore;

            if (history.size() > windowSize) {
                MatchResult removed = history.removeFirst();
                sumGoalsFor -= removed.goalsFor;
                sumGoalsAgainst -= removed.goalsAgainst;
                sumWinScore -= removed.winScore;
            }
        }

        double getAvgGoalsFor() {
            if (history.isEmpty()) {
                return DEFAULT_GOALS_FOR;
            }
            return sumGoalsFor / history.size();
        }

        double getAvgGoalsAgainst() {
            if (history.isEmpty()) {
                return DEFAULT_GOALS_AGAINST;
            }
            return sumGoalsAgainst / history.size();
        }

        double getAvgGoalDiff() {
            if (history.isEmpty()) {
                return 0.0;
            }
            return (sumGoalsFor - sumGoalsAgainst) / history.size();
        }

        double getWinRate() {
            if (history.isEmpty()) {
                return DEFAULT_WIN_RATE;
            }
            return sumWinScore / history.size();
        }
    }

    private static final class MatchResult {
        private final int goalsFor;
        private final int goalsAgainst;
        private final double winScore;

        private MatchResult(int goalsFor, int goalsAgainst) {
            this.goalsFor = goalsFor;
            this.goalsAgainst = goalsAgainst;
            if (goalsFor > goalsAgainst) {
                this.winScore = 1.0;
            } else if (goalsFor == goalsAgainst) {
                this.winScore = 0.5;
            } else {
                this.winScore = 0.0;
            }
        }
    }
}


