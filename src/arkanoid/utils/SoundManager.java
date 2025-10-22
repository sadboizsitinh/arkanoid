package arkanoid.utils;

import javafx.scene.media.AudioClip;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SoundManager {
    private static final Map<String, AudioClip> cache = new HashMap<>();
    private static AudioClip backgroundMusic;
    private static String currentBackgroundTrack = null; // ✅ Track hiện tại

    private static AudioClip loadSound(String fileName) {
        try {
            URL url = SoundManager.class.getResource("/arkanoid/assets/sounds/" + fileName);
            if (url == null) {
                System.err.println("❌ Không tìm thấy âm thanh: " + fileName);
                return null;
            }
            return new AudioClip(url.toString());
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi tải âm thanh " + fileName + ": " + e.getMessage());
            return null;
        }
    }

    private static AudioClip getSound(String fileName) {
        return cache.computeIfAbsent(fileName, SoundManager::loadSound);
    }

    /** Phát âm thanh hiệu ứng (ngắn) */
    public static void play(String fileName) {
        AudioClip clip = getSound(fileName);
        if (clip != null){
            clip.setVolume(0.6);
            clip.play();
        }
    }

    /** Phát nhạc nền loop vô hạn - CHỈ phát nếu chưa phát hoặc khác bài */
    public static void playBackground(String fileName, double volume) {
        // ✅ Nếu đang phát cùng bài nhạc thì KHÔNG làm gì
        if (fileName.equals(currentBackgroundTrack) &&
                backgroundMusic != null &&
                backgroundMusic.isPlaying()) {
            System.out.println("🎵 Background music already playing: " + fileName);
            return;
        }

        // ✅ Nếu khác bài thì dừng bài cũ và phát bài mới
        stopBackground();

        try {
            URL url = SoundManager.class.getResource("/arkanoid/assets/sounds/" + fileName);
            if (url != null) {
                backgroundMusic = new AudioClip(url.toString());
                backgroundMusic.setCycleCount(AudioClip.INDEFINITE);
                backgroundMusic.setVolume(volume);
                backgroundMusic.play();
                currentBackgroundTrack = fileName; // ✅ Lưu tên bài đang phát
                System.out.println("🎵 Started playing background: " + fileName);
            }
        } catch (Exception e) {
            System.err.println("❌ Không thể phát nhạc nền: " + e.getMessage());
        }
    }

    /** Dừng nhạc nền đang phát */
    public static void stopBackground() {
        if (backgroundMusic != null) {
            backgroundMusic.stop();
            currentBackgroundTrack = null; // ✅ Reset track name
            System.out.println("⏹️ Background music stopped");
        }
    }

    /** Tạm dừng nhạc nền */
    public static void pauseBackground() {
        if (backgroundMusic != null && backgroundMusic.isPlaying()) {
            backgroundMusic.stop();
            System.out.println("⏸️ Background music paused");
        }
    }

    /** Tiếp tục phát nhạc nền */
    public static void resumeBackground() {
        if (backgroundMusic != null) {
            backgroundMusic.play();
            System.out.println("▶️ Background music resumed");
        }
    }

    /** Kiểm tra xem có nhạc nền đang phát không */
    public static boolean isBackgroundPlaying() {
        return backgroundMusic != null && backgroundMusic.isPlaying();
    }

}