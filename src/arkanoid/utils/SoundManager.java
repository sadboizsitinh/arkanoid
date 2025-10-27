package arkanoid.utils;

import javafx.scene.media.AudioClip;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SoundManager {
    private static final Map<String, AudioClip> cache = new HashMap<>();
    private static AudioClip backgroundMusic;
    private static String currentBackgroundTrack = null;

    // MAP ÂM LƯỢNG CHO TỪNG FILE
    private static final Map<String, Double> volumeMap = new HashMap<>();
    static {
        // Âm thanh hiệu ứng
        volumeMap.put("paddle.wav", 0.5);      // Chạm paddle - nhỏ
        volumeMap.put("gach.wav", 0.5);        // Chạm gạch - nhỏ
        volumeMap.put("gachvo.wav", 0.6);      // Phá gạch - vừa
        volumeMap.put("powerup.wav", 0.7);     // Power-up - vừa to
        volumeMap.put("game_over.wav", 0.8);   // Game over - to
        volumeMap.put("matmang.wav", 0.7);     // Mất mạng - to
        volumeMap.put("Qua_man.wav", 0.8);     // Qua level - to
        volumeMap.put("steak.wav", 1.5);       // Streak/Excellent - to
    }

    private static AudioClip loadSound(String fileName) {
        try {
            URL url = SoundManager.class.getResource("/arkanoid/assets/sounds/" + fileName);
            if (url == null) {
                System.err.println("Không tìm thấy âm thanh: " + fileName);
                return null;
            }
            return new AudioClip(url.toString());
        } catch (Exception e) {
            System.err.println("Lỗi khi tải âm thanh " + fileName + ": " + e.getMessage());
            return null;
        }
    }

    private static AudioClip getSound(String fileName) {
        return cache.computeIfAbsent(fileName, SoundManager::loadSound);
    }

    /** Phát âm thanh hiệu ứng (ngắn) - SỬ DỤNG ÂM LƯỢNG TỪ MAP */
    public static void play(String fileName) {
        AudioClip clip = getSound(fileName);
        if (clip != null) {
            // LẤY ÂM LƯỢNG TỪ MAP, NẾU KHÔNG CÓ THÌ DÙNG MẶC ĐỊNH 0.6
            double volume = volumeMap.getOrDefault(fileName, 0.6);
            clip.setVolume(volume);
            clip.play();
        }
    }

    /** OVERLOAD: Phát âm thanh với âm lượng tùy chọn */
    public static void play(String fileName, double volume) {
        AudioClip clip = getSound(fileName);
        if (clip != null) {
            clip.setVolume(Math.max(0.0, Math.min(1.0, volume))); // Giới hạn 0-1
            clip.play();
        }
    }

    /** Phát nhạc nền loop vô hạn - CHỈ phát nếu chưa phát hoặc khác bài */
    public static void playBackground(String fileName, double volume) {
        // Nếu đang phát cùng bài nhạc thì KHÔNG làm gì
        if (fileName.equals(currentBackgroundTrack) &&
                backgroundMusic != null &&
                backgroundMusic.isPlaying()) {
            System.out.println("🎵 Background music already playing: " + fileName);
            return;
        }

        // Nếu khác bài thì dừng bài cũ và phát bài mới
        stopBackground();

        try {
            URL url = SoundManager.class.getResource("/arkanoid/assets/sounds/" + fileName);
            if (url != null) {
                backgroundMusic = new AudioClip(url.toString());
                backgroundMusic.setCycleCount(AudioClip.INDEFINITE);

                // GIỚI HẠN ÂM LƯỢNG NHẠC NỀN (0-0.4 để không quá to)
                double limitedVolume = Math.max(0.0, Math.min(0.4, volume));
                backgroundMusic.setVolume(limitedVolume);
                backgroundMusic.play();
                currentBackgroundTrack = fileName;
                System.out.println("🎵 Started playing background: " + fileName + " (volume: " + limitedVolume + ")");
            }
        } catch (Exception e) {
            System.err.println("Không thể phát nhạc nền: " + e.getMessage());
        }
    }

    /** Dừng nhạc nền đang phát */
    public static void stopBackground() {
        if (backgroundMusic != null) {
            backgroundMusic.stop();
            currentBackgroundTrack = null;
            System.out.println("Background music stopped");
        }
    }

    /** Tạm dừng nhạc nền */
    public static void pauseBackground() {
        if (backgroundMusic != null && backgroundMusic.isPlaying()) {
            backgroundMusic.stop();
            System.out.println("Background music paused");
        }
    }

    /** Tiếp tục phát nhạc nền */
    public static void resumeBackground() {
        if (backgroundMusic != null) {
            backgroundMusic.play();
            System.out.println("Background music resumed");
        }
    }

    /** Kiểm tra xem có nhạc nền đang phát không */
    public static boolean isBackgroundPlaying() {
        return backgroundMusic != null && backgroundMusic.isPlaying();
    }

    /** CHỈNH ÂM LƯỢNG CỦA NHẠC NỀN */
    public static void setBackgroundVolume(double volume) {
        if (backgroundMusic != null) {
            double limitedVolume = Math.max(0.0, Math.min(0.3, volume)); // Max 40%
            backgroundMusic.setVolume(limitedVolume);
            System.out.println(" Background volume set to: " + limitedVolume);
        }
    }

}