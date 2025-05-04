package io.github.bl3rune.blu3printPlugin.enums;

public enum GConfig {
    MAX_SCALE,
    MAX_SIZE,
    MAX_OVERALL_SIZE,
    COOLDOWN,
    HOLOGRAM_TTL,
    ALIGNMENT("placement.alignment"),
    RELATIVE("placement.relative"),
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
