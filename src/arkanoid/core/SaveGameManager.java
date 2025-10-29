package arkanoid.core;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * (FILE HOÀN CHỈNH)
 * Singleton manager để quản lý các lượt lưu game (profile).
 * Giới hạn 10 lượt lưu.
 */
public class SaveGameManager {
    private static SaveGameManager instance;
    private static final String SAVE_FILE = "player_saves.dat";
    private static final int MAX_SAVES = 10;

    // [SỬA LỖI] Tên biến đúng là "savedGames" (không có chữ "d")
    private List<PlayerSave> savedGames;

    private SaveGameManager() {
        savedGames = new ArrayList<>();
        loadSaveGames();
    }

    public static synchronized SaveGameManager getInstance() {
        if (instance == null) {
            instance = new SaveGameManager();
        }
        return instance;
    }

    /**
     * Lấy danh sách tất cả các lượt lưu (đã sắp xếp)
     */
    public List<PlayerSave> getSavedGames() {
        Collections.sort(savedGames);
        return new ArrayList<>(savedGames);
    }

    /**
     * Kiểm tra xem có file save nào không
     */
    public boolean hasSavedGames() {
        return !savedGames.isEmpty();
    }

    /**
     * Lưu game state hiện tại với tên người chơi.
     */
    public void saveGame(String playerName, GameStateSnapshot snapshot) {
        if (playerName == null || playerName.trim().isEmpty()) {
            System.err.println("Player name is empty, cannot save.");
            return;
        }

        PlayerSave newSave = new PlayerSave(playerName, snapshot);

        // Xóa lượt lưu cũ nếu tên bị trùng
        savedGames.removeIf(save -> save.getPlayerName().equalsIgnoreCase(playerName.trim()));
        savedGames.add(0, newSave);
        Collections.sort(savedGames);

        while (savedGames.size() > MAX_SAVES) {
            savedGames.remove(savedGames.size() - 1); // Xóa cái cũ nhất
        }

        saveSaveGamesToFile(); // Gọi hàm save (đã thêm ở dưới)
        System.out.println("Game saved for player: " + playerName);
    }

    /**
     * (HÀM BỊ THIẾU MÀ BẠN CẦN)
     * Xóa một lượt lưu (profile) dựa theo tên người chơi.
     */
    public void deleteSave(String playerName) {
        if (playerName == null || playerName.isEmpty()) {
            return;
        }

        // [SỬA LỖI] Dùng "savedGames"
        boolean removed = savedGames.removeIf(save ->
                save.getPlayerName().equalsIgnoreCase(playerName)
        );

        if (removed) {
            System.out.println("Đã xóa profile: " + playerName);
            saveSaveGamesToFile(); // Gọi hàm save (đã thêm ở dưới)
        } else {
            System.out.println("Không tìm thấy profile để xóa: " + playerName);
        }
    }

    /**
     * (HÀM BỊ THIẾU)
     * Đọc danh sách save từ file
     */
    @SuppressWarnings("unchecked")
    private void loadSaveGames() {
        Path filePath = Paths.get(SAVE_FILE);
        if (!Files.exists(filePath)) {
            System.out.println("No player saves file found, starting fresh");
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(filePath.toFile()))) {
            savedGames = (List<PlayerSave>) ois.readObject();
            Collections.sort(savedGames);
            System.out.println("Loaded " + savedGames.size() + " player saves");
        } catch (Exception e) {
            System.err.println("Error loading player saves: " + e.getMessage());
            savedGames = new ArrayList<>();
        }
    }

    /**
     * (HÀM BỊ THIẾU)
     * Ghi danh sách save hiện tại vào file
     */
    private void saveSaveGamesToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(SAVE_FILE))) {
            oos.writeObject(savedGames);
            System.out.println("Player saves list saved to file");
        } catch (Exception e) {
            System.err.println("Error saving player saves: " + e.getMessage());
        }
    }
}