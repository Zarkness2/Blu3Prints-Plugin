package io.github.bl3rune.blu3printPlugin.enums;

public enum GConfig {
    MAX_SCALE,
    MAX_SIZE,
    MAX_OVERALL_SIZE,
    COOLDOWN,
    HOLOGRAM_TTL,
    ALIGNMENT("placement.alignment"),
    RELATIVE("placement.relative"),
    FREE_PLACEMENT_MESSAGE("messaging.free-placement-message.enabled"),
    FORCED_PLACEMENT_MESSAGE("messaging.force-placement-message.enabled"),
    DISCOUNT_PLACEMENT_MESSAGE("messaging.discount-placement-message.enabled"),
    UPDATE_AVAILABLE_MESSAGE("messaging.update-available-message.enabled"),
    COOLDOWN_MESSAGE("messaging.cooldown-message.enabled"),
    ;

    private final String configPath;

    GConfig(String path) {
        this.configPath = "blu3print." + path.toLowerCase();
    }

    GConfig() {
        this.configPath = "blu3print." + name().toLowerCase().replace('_', '-');
    }

    public String getConfigPath() {
        return configPath;
    }

}
