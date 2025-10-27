package arkanoid.core;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Singleton manager for high scores
 * Handles loading, saving, and managing high score entries
 */
public class HighScoreManager {
    private static HighScoreManager instance;
    private static final String SAVE_FILE = "highscores.dat";
    private static final int MAX_SCORES = 10; // Top 10

    private List<HighScore> highScores;

    private HighScoreManager() {
        highScores = new ArrayList<>();
        loadHighScores();
    }

    public static HighScoreManager getInstance() {
        if (instance == null) {
            instance = new HighScoreManager();
        }
        return instance;
    }

    /**
     * Kiểm tra xem score có đủ điều kiện vào bảng xếp hạng không
     */
    public boolean isHighScore(int score) {
        if (highScores.size() < MAX_SCORES) {
            return true;
        }
        return score > highScores.get(highScores.size() - 1).getScore();
    }

    /**
     * Thêm high score mới
     * @return rank của score mới (1-based), hoặc -1 nếu không vào top
     */
    public int addHighScore(String playerName, int score, int level) {
        // Validate input
        if (playerName == null || playerName.trim().isEmpty()) {
            playerName = "Anonymous";
        }
        playerName = playerName.trim();
        if (playerName.length() > 15) {
            playerName = playerName.substring(0, 15);
        }

        HighScore newScore = new HighScore(playerName, score, level);
        highScores.add(newScore);
        Collections.sort(highScores);

        // Giữ chỉ top MAX_SCORES
        if (highScores.size() > MAX_SCORES) {
            highScores = highScores.subList(0, MAX_SCORES);
        }

        saveHighScores();

        // Tìm rank của score mới
        for (int i = 0; i < highScores.size(); i++) {
            if (highScores.get(i) == newScore) {
                return i + 1;
            }
        }
        return -1;
    }

    /**
     * Lấy danh sách high scores
     */
    public List<HighScore> getHighScores() {
        return new ArrayList<>(highScores);
    }

    /**
     * Xóa tất cả high scores
     */
    public void clearHighScores() {
        highScores.clear();
        saveHighScores();
    }

    /**
     * Load high scores từ file
     */
    @SuppressWarnings("unchecked")
    private void loadHighScores() {
        try {
            Path filePath = Paths.get(SAVE_FILE);
            if (!Files.exists(filePath)) {
                System.out.println("No high scores file found, starting fresh");
                return;
            }

            try (ObjectInputStream ois = new ObjectInputStream(
                    new FileInputStream(filePath.toFile()))) {
                highScores = (List<HighScore>) ois.readObject();
                Collections.sort(highScores);
                System.out.println("Loaded " + highScores.size() + " high scores");
            }

        } catch (Exception e) {
            System.err.println("Error loading high scores: " + e.getMessage());
            highScores = new ArrayList<>();
        }
    }

    /**
     * Lưu high scores vào file
     */
    private void saveHighScores() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(SAVE_FILE))) {
            oos.writeObject(highScores);
            System.out.println("High scores saved");
        } catch (Exception e) {
            System.err.println("Error saving high scores: " + e.getMessage());
        }
    }

    /**
     * Get rank of a score (không thêm vào list)
     */
    public int getRankForScore(int score) {
        for (int i = 0; i < highScores.size(); i++) {
            if (score > highScores.get(i).getScore()) {
                return i + 1;
            }
        }
        if (highScores.size() < MAX_SCORES) {
            return highScores.size() + 1;
        }
        return -1; // Không vào top
    }
}