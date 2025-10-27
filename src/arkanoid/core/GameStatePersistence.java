package arkanoid.core;

import java.io.*;
import java.nio.file.*;

/**
 * Lưu và đọc game state từ file
 */
public class GameStatePersistence {
    private static final String SAVE_FILE = "arkanoid_save.dat";

    /**
     * Lưu game state vào file
     */
    public static boolean saveToFile(GameStateSnapshot snapshot) {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(SAVE_FILE))) {
            oos.writeObject(snapshot);
            System.out.println("Game saved to file: " + SAVE_FILE);
            return true;
        } catch (IOException e) {
            System.err.println("Error saving game: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Đọc game state từ file
     */
    public static GameStateSnapshot loadFromFile() {
        File saveFile = new File(SAVE_FILE);

        if (!saveFile.exists()) {
            System.out.println("No save file found");
            return null;
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(SAVE_FILE))) {
            GameStateSnapshot snapshot = (GameStateSnapshot) ois.readObject();
            System.out.println("Game loaded from file: " + SAVE_FILE);
            System.out.println("   Score: " + snapshot.score + ", Lives: " + snapshot.lives + ", Level: " + snapshot.level);
            return snapshot;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading game: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Xóa file save
     */
    public static void deleteSaveFile() {
        try {
            Files.deleteIfExists(Paths.get(SAVE_FILE));
            System.out.println("Save file deleted");
        } catch (IOException e) {
            System.err.println("Error deleting save file: " + e.getMessage());
        }
    }

    /**
     * Kiểm tra có file save không
     */
    public static boolean hasSaveFile() {
        return new File(SAVE_FILE).exists();
    }
}