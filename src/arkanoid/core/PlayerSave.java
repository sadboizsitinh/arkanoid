package arkanoid.core;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * (FILE HOÀN CHỈNH)
 * Đại diện cho một lượt lưu game của người chơi (một "profile").
 */
public class PlayerSave implements Serializable, Comparable<PlayerSave> {
    private static final long serialVersionUID = 1L;

    private String playerName;
    private String saveDate;
    private GameStateSnapshot snapshot; // Dữ liệu game được lưu

    public PlayerSave(String playerName, GameStateSnapshot snapshot) {
        this.playerName = playerName;
        this.snapshot = snapshot;
        this.saveDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    // --- CÁC HÀM GETTER BỊ THIẾU ---
    public String getPlayerName() { return playerName; }
    public String getSaveDate() { return saveDate; }
    public GameStateSnapshot getSnapshot() { return snapshot; }

    /**
     * Dùng để sắp xếp, lượt lưu mới nhất sẽ ở trên cùng.
     */
    @Override
    public int compareTo(PlayerSave other) {
        try {
            LocalDateTime thisDate = LocalDateTime.parse(this.saveDate, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            LocalDateTime otherDate = LocalDateTime.parse(other.saveDate, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            return otherDate.compareTo(thisDate); // Sắp xếp giảm dần (mới nhất trước)
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Format để hiển thị trong ListView/ChoiceDialog
     */
    @Override
    public String toString() {
        return String.format("%s (Level: %d, Score: %d) - %s",
                playerName,
                (snapshot != null ? snapshot.level : 0),
                (snapshot != null ? snapshot.score : 0),
                saveDate
        );
    }
}