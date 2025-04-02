package io.github.bl3rune.blu3printPlugin.config;

import java.util.Arrays;
import java.util.List;

import io.github.bl3rune.blu3printPlugin.Blu3PrintPlugin;

public class Blu3printConfiguration {
    
    private static Integer maxSize = null;
    private static Integer maxScale = null;
    private static Integer maxOverallSize = null;
    private static Integer cooldown = null;
    private static List<String> ignoredMaterials;

    public static void refreshConfiguration() {
        maxSize = tryAndGetConfig("blu3print.max-size");
        maxScale = tryAndGetConfig("blu3print.max-scale");
        maxOverallSize = tryAndGetConfig("blu3print.max-overall-size");
        cooldown = tryAndGetConfig("blu3print.cooldown");
        ignoredMaterials = tryAndGetConfigList("blu3print.ignored-materials");
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

    public static List<String> getIgnoredMaterials() {
        return ignoredMaterials;
    }

}
