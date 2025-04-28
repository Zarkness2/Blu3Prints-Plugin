package io.github.bl3rune.blu3printPlugin.config;

import java.util.Arrays;
import java.util.List;

import io.github.bl3rune.blu3printPlugin.Blu3PrintPlugin;
import io.github.bl3rune.blu3printPlugin.enums.Alignment;
import io.github.bl3rune.blu3printPlugin.enums.SemanticLevel;

import static io.github.bl3rune.blu3printPlugin.Blu3PrintPlugin.logger;

public class Blu3printConfiguration {
    
    private static Integer maxSize = null;
    private static Integer maxScale = null;
    private static Integer maxOverallSize = null;
    private static Integer cooldown = null;
    private static Integer hologramTtl = null;
    private static List<String> ignoredMaterials;
    // Placement
    private static Alignment alignment = null;
    private static boolean relativePlacement = false;
    // Messages
    private static boolean freePlacementMessageEnabled = false;
    private static boolean forcePlacementMessageEnabled = false;
    private static boolean discountPlacementMessageEnabled = false;
    private static boolean updateAvailableMessageEnabled = false;
    private static boolean cooldownMessageEnabled = false;
    // Logging
    private static boolean verboseLogging = false;
    private static boolean importedBlu3printsLoggingEnabled = false;
    private static SemanticLevel updateLoggingLevel = SemanticLevel.MINOR;
    // Other
    private static Integer updateCheckInterval = null;

    public static void refreshConfiguration() {
        verboseLogging = tryAndGetConfigFlag("blu3print.logging.verbose");

        maxSize = tryAndGetConfigInteger("blu3print.max-size");
        maxScale = tryAndGetConfigInteger("blu3print.max-scale");
        maxOverallSize = tryAndGetConfigInteger("blu3print.max-overall-size");
        cooldown = tryAndGetConfigInteger("blu3print.cooldown");
        hologramTtl = tryAndGetConfigInteger("blu3print.hologram-ttl");
        ignoredMaterials = tryAndGetConfigList("blu3print.ignored-materials");
        // Placement settings
        alignment = tryAndGetConfigEnum("blu3print.placement.alignment", Alignment.class);
        relativePlacement = tryAndGetConfigFlag("blu3print.placement.relative");
        // Message settings
        freePlacementMessageEnabled = tryAndGetConfigFlag("blu3print.messaging.free-placement-message.enabled");
        forcePlacementMessageEnabled = tryAndGetConfigFlag("blu3print.messaging.force-placement-message.enabled");
        discountPlacementMessageEnabled = tryAndGetConfigFlag("blu3print.messaging.discount-placement-message.enabled");
        updateAvailableMessageEnabled = tryAndGetConfigFlag("blu3print.messaging.update-available-message.enabled");
        cooldownMessageEnabled = tryAndGetConfigFlag("blu3print.messaging.cooldown-message.enabled");
        // Logging settings
        importedBlu3printsLoggingEnabled = tryAndGetConfigFlag("blu3print.logging.imported-blu3prints.enabled");
        updateLoggingLevel = tryAndGetConfigEnum("blu3print.logging.update-level", SemanticLevel.class);

        // Other settings
        updateCheckInterval = tryAndGetConfigInteger("blu3print.update-check-interval");


    }

    private static Integer tryAndGetConfigInteger(String key) {
        try {
            String value = Blu3PrintPlugin.getBlu3PrintPlugin().getConfig().getString(key, null);
            return value == null ? null : Integer.parseInt(value);
        } catch (Exception e) {
            return null;
        }
    }

    private static <T extends Enum<T>> T tryAndGetConfigEnum(String key, Class<T> clazz) {
        try {
            if (clazz.isEnum()) {
                String value = Blu3PrintPlugin.getBlu3PrintPlugin().getConfig().getString(key, null);
                if (isVerboseLogging()) {
                    logger().info("Config check : " + clazz.getName() + " : " + value);
                }
                for  (T e : clazz.getEnumConstants()) {
                    if (e.name().equalsIgnoreCase(value)) {
                        if (isVerboseLogging()) {
                            logger().info("Found : " + clazz.getName() + " : " + e.name());
                        }
                        return e;
                    }
                }
            }
            return null;
        } catch (Exception e) {
            logger().info("Failed to get Enum " + clazz.getName());
            if (isVerboseLogging()) {
                e.printStackTrace();
            }
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

    public static Alignment getAlignment() {
        if (alignment == null) {
            return Alignment.CENTER;
        }
        return alignment;
    }

    public static boolean getRelativePlacement() {
        return relativePlacement;
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

    public static boolean isUpdateAvailableMessageEnabled() {
        return updateAvailableMessageEnabled;
    }

    public static boolean isCooldownMessageEnabled() {
        return cooldownMessageEnabled;
    }

    public static boolean isVerboseLogging() {
        return verboseLogging;
    }

    public static boolean isImportedBlu3printsLoggingEnabled() {
        return importedBlu3printsLoggingEnabled;
    }

    public static SemanticLevel getUpdateLoggingLevel() {
        return updateLoggingLevel;
    }

    public static int getUpdateCheckInterval() {
        if (updateCheckInterval == null) {
            return 24;
        }
        return updateCheckInterval;
    }
}
