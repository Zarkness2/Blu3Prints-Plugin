package io.github.bl3rune.blu3printPlugin.config;

import io.github.bl3rune.blu3printPlugin.Blu3PrintPlugin;

public class Blu3printConfiguration {
    
    private static Integer maxSize = null;
    private static Integer maxScale = null;
    private static Integer maxOverallSize = null;
    private static Integer cooldown = null;

    public static void refreshConfiguration() {
        maxSize = tryAndGetConfig("blu3print.max-size");
        maxScale = tryAndGetConfig("blu3print.max-scale");
        maxOverallSize = tryAndGetConfig("blu3print.max-overall-size");
        cooldown = tryAndGetConfig("blu3print.cooldown");
    }

    private static Integer tryAndGetConfig(String key) {
        try {
            String value = Blu3PrintPlugin.getBlu3PrintPlugin().getConfig().getString(key, null);
            return value == null ? null : Integer.parseInt(value);
        } catch (Exception e) {
            return null;
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

}
