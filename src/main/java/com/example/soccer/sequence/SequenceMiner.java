package com.example.soccer.sequence;

import com.example.soccer.db.SQLiteConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Simple sequence mining over team match outcomes.
 * Builds per-team sequences of W/D/L ordered by match date and finds
 * the most frequent n-gram patterns.
 */
public final class SequenceMiner {

    private SequenceMiner() {
    }

    /**
     * Build ordered sequences of outcomes for each team.
     * W = win, D = draw, L = loss from that team's perspective.
     */
    public static Map<Integer, List<Character>> buildTeamSequences() throws SQLException {
        String sql = """
                SELECT
                    date,
                    home_team_api_id,
                    away_team_api_id,
                    home_team_goal,
                    away_team_goal
                FROM Match
                ORDER BY date
                """;

        Map<Integer, List<Character>> sequences = new HashMap<>();

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int homeTeamId = rs.getInt(2);
                int awayTeamId = rs.getInt(3);
                int homeGoals = rs.getInt(4);
                int awayGoals = rs.getInt(5);

                char homeOutcome;
                char awayOutcome;
                if (homeGoals > awayGoals) {
                    homeOutcome = 'W';
                    awayOutcome = 'L';
                } else if (homeGoals < awayGoals) {
                    homeOutcome = 'L';
                    awayOutcome = 'W';
                } else {
                    homeOutcome = 'D';
                    awayOutcome = 'D';
                }

                sequences.computeIfAbsent(homeTeamId, id -> new ArrayList<>()).add(homeOutcome);
                sequences.computeIfAbsent(awayTeamId, id -> new ArrayList<>()).add(awayOutcome);
            }
        }

        return sequences;
    }

    /**
     * Mine frequent n-gram patterns across all team sequences.
     */
    public static Map<String, Integer> mineNGrams(Map<Integer, List<Character>> sequences, int n) {
        Map<String, Integer> counts = new HashMap<>();
        for (List<Character> seq : sequences.values()) {
            if (seq.size() < n) continue;
            for (int i = 0; i <= seq.size() - n; i++) {
                StringBuilder sb = new StringBuilder(n);
                for (int j = 0; j < n; j++) {
                    sb.append(seq.get(i + j));
                }
                String pattern = sb.toString();
                counts.merge(pattern, 1, Integer::sum);
            }
        }
        return counts;
    }

    public static void printTopPatterns(int n, int topK) throws SQLException {
        System.out.println();
        System.out.println("========================================");
        System.out.println("SEQUENCE MINING: TOP " + topK + " PATTERNS (n=" + n + ")");
        System.out.println("========================================");

        Map<Integer, List<Character>> sequences = buildTeamSequences();
        Map<String, Integer> counts = mineNGrams(sequences, n);

        List<Map.Entry<String, Integer>> list = new ArrayList<>(counts.entrySet());
        list.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        int printed = 0;
        for (Map.Entry<String, Integer> e : list) {
            System.out.printf("Pattern %s : %d occurrences%n", e.getKey(), e.getValue());
            printed++;
            if (printed >= topK) break;
        }
    }
}



