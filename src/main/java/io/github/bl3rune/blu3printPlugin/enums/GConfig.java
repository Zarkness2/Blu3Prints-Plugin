package io.github.bl3rune.blu3printPlugin.enums;

import java.util.function.Supplier;

import io.github.bl3rune.blu3printPlugin.config.GlobalConfig;

public enum GConfig {
    MAX_SCALE(GlobalConfig::getMaxScale),
    MAX_SIZE(GlobalConfig::getMaxSize),
    MAX_OVERALL_SIZE(GlobalConfig::getMaxOverallSize),
    COOLDOWN(GlobalConfig::getCooldown),
    HOLOGRAM_TTL(GlobalConfig::getHologramTtl),
    ALIGNMENT(GlobalConfig::getAlignment, "placement.alignment"),
    RELATIVE(GlobalConfig::getRelativePlacement, "placement.relative"),
    // Messaging
    FREE_PLACEMENT_MESSAGE(GlobalConfig::isFreePlacementMessageEnabled, "messaging.free-placement-message.enabled"),
    FORCED_PLACEMENT_MESSAGE(GlobalConfig::isForcePlacementMessageEnabled, "messaging.force-placement-message.enabled"),
    DISCOUNT_PLACEMENT_MESSAGE(GlobalConfig::isDiscountPlacementMessageEnabled, "messaging.discount-placement-message.enabled"),
    UPDATE_AVAILABLE_MESSAGE(GlobalConfig::isUpdateAvailableMessageEnabled, "messaging.update-available-message.enabled"),
    COOLDOWN_MESSAGE(GlobalConfig::isCooldownMessageEnabled, "messaging.cooldown-message.enabled"),
    // Logging
    VERBOSE_LOGGING(GlobalConfig::isVerboseLogging, "logging.verbose"),
    IMPORTED_BLU3PRINTS_LOGGING(GlobalConfig::isImportedBlu3printsLoggingEnabled, "logging.imported-blu3prints.enabled"),
    UPDATE_LEVEL(GlobalConfig::getUpdateLoggingLevel, "logging.update-level"),
    //  Other
    UPDATE_CHECK_INTERVAL(GlobalConfig::getUpdateCheckInterval),
    
    ;

    private final String configPath;
    private final Supplier<Object> getter;

    GConfig(Supplier<Object> getter, String path) {
        this.getter = getter;
        this.configPath = "blu3print." + path.toLowerCase();
    }

    GConfig(Supplier<Object> getter) {
        this.getter = getter;
        this.configPath = "blu3print." + name().toLowerCase().replace('_', '-');
    }

    public String getConfigPath() {
        return configPath;
    }

    public String getCurrentValue() {
        return "" + getter.get();
    }
}
