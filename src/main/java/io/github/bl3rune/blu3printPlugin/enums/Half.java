package io.github.bl3rune.blu3printPlugin.enums;

public enum Half {

    TOP(org.bukkit.block.data.Bisected.Half.TOP),
    BOTTOM(org.bukkit.block.data.Bisected.Half.BOTTOM);

    private org.bukkit.block.data.Bisected.Half half;
    private String fullName;
    private String shortName;

    Half(org.bukkit.block.data.Bisected.Half half) {
        this.half = half;
        this.fullName = "half=" + half.name().toLowerCase();
        this.shortName = "h=" + half.name().toLowerCase().substring(0, 1);
    }
    
    public org.bukkit.block.data.Bisected.Half getHalf() {
        return half;
    }
    public String getFullName() {
        return fullName;
    }
    public String getShortName() {
        return shortName;
    }

    public static Half fromBukkit(org.bukkit.block.data.Bisected.Half half) {
        for (Half bisected : values()) {
            if (bisected.getHalf() == half) {
                return bisected;
            }
        }
        return null;
    }

    public static Half findInComplexDataString(String s) {
        for (Half bisected : values()) {
            if (bisected.getFullName().equalsIgnoreCase(s) || bisected.getShortName().equalsIgnoreCase(s)) {
                return bisected;
            }
        }
        return null;
    }

}
