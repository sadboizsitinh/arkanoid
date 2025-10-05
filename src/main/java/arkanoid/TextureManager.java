package arkanoid;

import javafx.scene.image.Image;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton class to manage game textures
 * Caches loaded images to improve performance
 */
public class TextureManager {
    private static TextureManager instance;
    private Map<String, Image> textureCache;

    private TextureManager() {
        textureCache = new HashMap<>();
    }

    public static TextureManager getInstance() {
        if (instance == null) {
            instance = new TextureManager();
        }
        return instance;
    }

    /**
     * Load texture from resources folder
     * @param resourcePath Path relative to resources folder (e.g., "/images/paddle.png")
     */
    public Image loadTexture(String resourcePath) {
        // Check cache first
        if (textureCache.containsKey(resourcePath)) {
            return textureCache.get(resourcePath);
        }

        try {
            Image image = new Image(getClass().getResourceAsStream(resourcePath));
            textureCache.put(resourcePath, image);
            System.out.println("Loaded texture: " + resourcePath);
            return image;
        } catch (Exception e) {
            System.err.println("Failed to load texture: " + resourcePath + " - " + e.getMessage());
            return null;
        }
    }

    /**
     * Load texture from file system path
     * @param filePath Absolute or relative file path
     */
    public Image loadTextureFromFile(String filePath) {
        if (textureCache.containsKey(filePath)) {
            return textureCache.get(filePath);
        }

        try {
            Image image = new Image("file:" + filePath);
            textureCache.put(filePath, image);
            System.out.println("Loaded texture from file: " + filePath);
            return image;
        } catch (Exception e) {
            System.err.println("Failed to load texture from file: " + filePath + " - " + e.getMessage());
            return null;
        }
    }

    /**
     * Clear texture cache
     */
    public void clearCache() {
        textureCache.clear();
        System.out.println("Texture cache cleared");
    }

    /**
     * Get cached texture
     */
    public Image getTexture(String path) {
        return textureCache.get(path);
    }
}