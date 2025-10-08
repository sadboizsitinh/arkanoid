package arkanoid;

import javafx.scene.image.Image;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Singleton TextureManager for loading and caching game textures
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
     * Load texture from resources, cache it if not already loaded
     */
    public Image loadTexture(String resourcePath) {
        if (textureCache.containsKey(resourcePath)) {
            return textureCache.get(resourcePath);
        }

        try {
            Image image = new Image(getClass().getResourceAsStream(resourcePath));
            if (!image.isError()) {
                textureCache.put(resourcePath, image);
                return image;
            } else {
                System.err.println("Error loading texture: " + resourcePath);
                return null;
            }
        } catch (Exception e) {
            System.err.println("Failed to load texture " + resourcePath + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Get texture from cache
     */
    public Image getTexture(String resourcePath) {
        return textureCache.get(resourcePath);
    }

    /**
     * Check if texture is loaded
     */
    public boolean isLoaded(String resourcePath) {
        return textureCache.containsKey(resourcePath);
    }

    /**
     * Clear all cached textures
     */
    public void clearCache() {
        textureCache.clear();
    }

    /**
     * Get cache size
     */
    public int getCacheSize() {
        return textureCache.size();
    }
}