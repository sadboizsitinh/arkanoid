package arkanoid.core;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a single high score entry
 */
public class HighScore implements Serializable, Comparable<HighScore> {
    private static final long serialVersionUID = 1L;

    private String playerName;
    private int score;
    private int level;
    private String date;

    public HighScore(String playerName, int score, int level) {
        this.playerName = playerName;
        this.score = score;
        this.level = level;
        this.date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    // Getters
    public String getPlayerName() { return playerName; }
    public int getScore() { return score; }
    public int getLevel() { return level; }
    public String getDate() { return date; }

    @Override
    public int compareTo(HighScore other) {
        // Sắp xếp giảm dần theo score
        int scoreCompare = Integer.compare(other.score, this.score);
        if (scoreCompare != 0) return scoreCompare;

        // Nếu score bằng nhau, xếp theo level
        return Integer.compare(other.level, this.level);
    }

    @Override
    public String toString() {
        return String.format("%s - Score: %d - Level: %d - %s",
                playerName, score, level, date);
    }

    /**
     * Format for display in ListView
     */
    public String toDisplayString(int rank) {
        return String.format("%d. %-15s %6d pts (Lvl %d)",
                rank, playerName, score, level);
    }
}