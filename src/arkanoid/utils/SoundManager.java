package arkanoid.utils;

import javafx.scene.media.AudioClip;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Quản lý toàn bộ âm thanh game: nhạc nền + hiệu ứng.
 */
public class SoundManager {
    private static final Map<String, AudioClip> cache = new HashMap<>();
    private static AudioClip backgroundMusic;

    private static AudioClip loadSound(String fileName) {
        try {
            URL url = SoundManager.class.getResource("/arkanoid/assets/sounds/" + fileName);
            if (url == null) {
                System.err.println(" Không tìm thấy âm thanh: " + fileName);
                return null;
            }
            return new AudioClip(url.toString());
        } catch (Exception e) {
            System.err.println(" Lỗi khi tải âm thanh " + fileName + ": " + e.getMessage());
            return null;
        }
    }

    private static AudioClip getSound(String fileName) {
        return cache.computeIfAbsent(fileName, SoundManager::loadSound);
    }

    /** Phát âm thanh hiệu ứng (ngắn) */
    public static void play(String fileName) {
        AudioClip clip = getSound(fileName);
        if (clip != null) clip.play();
    }

    /** Phát nhạc nền loop vô hạn */
    public static void playBackground(String fileName, double volume) {
        stopBackground();
        try {
            URL url = SoundManager.class.getResource("/arkanoid/assets/sounds/" + fileName);
            if (url != null) {
                backgroundMusic = new AudioClip(url.toString());
                backgroundMusic.setCycleCount(AudioClip.INDEFINITE);
                backgroundMusic.setVolume(volume);
                backgroundMusic.play();
            }
        } catch (Exception e) {
            System.err.println(" Không thể phát nhạc nền: " + e.getMessage());
        }
    }

    public static void stopBackground() {
        if (backgroundMusic != null) {
            backgroundMusic.stop();
            backgroundMusic = null;
        }
    }
}
