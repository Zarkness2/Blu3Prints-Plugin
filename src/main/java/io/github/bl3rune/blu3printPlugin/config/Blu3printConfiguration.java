package io.github.bl3rune.blu3printPlugin.config;

import java.util.Arrays;
import java.util.List;

import io.github.bl3rune.blu3printPlugin.Blu3PrintPlugin;

public class Blu3printConfiguration {
    
    private static Integer maxSize = null;
    private static Integer maxScale = null;
    private static Integer maxOverallSize = null;
    private static Integer cooldown = null;
    private static Integer hologramTtl = null;
    private static List<String> ignoredMaterials;
    private static boolean freePlacementMessageEnabled = false;
    private static boolean forcePlacementMessageEnabled = false;
    private static boolean discountPlacementMessageEnabled = false;

    public static void refreshConfiguration() {
        maxSize = tryAndGetConfig("blu3print.max-size");
        maxScale = tryAndGetConfig("blu3print.max-scale");
        maxOverallSize = tryAndGetConfig("blu3print.max-overall-size");
        cooldown = tryAndGetConfig("blu3print.cooldown");
        hologramTtl = tryAndGetConfig("blu3print.hologram-ttl");
        ignoredMaterials = tryAndGetConfigList("blu3print.ignored-materials");
        // Message settings
        freePlacementMessageEnabled = tryAndGetConfigFlag("blu3print.messaging.free-placement-message.enabled");
        forcePlacementMessageEnabled = tryAndGetConfigFlag("blu3print.messaging.force-placement-message.enabled");
        discountPlacementMessageEnabled = tryAndGetConfigFlag("blu3print.messaging.discount-placement-message.enabled");
    }

    private static Integer tryAndGetConfig(String key) {
        try {
            String value = Blu3PrintPlugin.getBlu3PrintPlugin().getConfig().getString(key, null);
            return value == null ? null : Integer.parseInt(value);
        } catch (Exception e) {
            return null;
        }
    }

    private static List<String> tryAndGetConfigList(String key) {
        try {
            return Blu3PrintPlugin.getBlu3PrintPlugin().getConfig().getStringList(key);
        } catch (Exception e) {
            return Arrays.asList("AIR");
        }
    }

    private static boolean tryAndGetConfigFlag(String key) {
        try {
            return Blu3PrintPlugin.getBlu3PrintPlugin().getConfig().getBoolean(key, false);
        } catch (Exception e) {
            return false;
        }
    }

    public static Integer getMaxSize() {
        return maxSize;
    }

    public static Integer getMaxScale() {
        return maxScale;
    }

    public static Integer getMaxOverallSize() {
        return maxOverallSize;
    }

    public static Integer getCooldown() {
        if (cooldown == null) {
            return 0;
        }
        return cooldown;
    }

    public static Integer getHologramTtl() {
        if (hologramTtl == null) {
            return 10;
        }
        return hologramTtl;
    }

    public static List<String> getIgnoredMaterials() {
        return ignoredMaterials;
    }

    public static boolean isFreePlacementMessageEnabled() {
        return freePlacementMessageEnabled;
    }

    public static boolean isForcePlacementMessageEnabled() {
        return forcePlacementMessageEnabled;
    }

    public static boolean isDiscountPlacementMessageEnabled() {
        return discountPlacementMessageEnabled;
    }

}
