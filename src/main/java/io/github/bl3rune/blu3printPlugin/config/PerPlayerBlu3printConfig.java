package io.github.bl3rune.blu3printPlugin.config;

public class PerPlayerBlu3printConfig {

    private String uuid; // Blu3print to apply config to
    private int [] [] hologramViewLayers; // X[], Y[], Z[]

    public PerPlayerBlu3printConfig(String uuid) {
        this.uuid = uuid;
        this.hologramViewLayers = null;
    }

    public boolean uuidMatches(String uuid) {
        if (this.uuid == null) return false;
        return this.uuid.equals(uuid);
    }

    public int [] [] getHologramViewLayers() {
        if (hologramViewLayers == null) {
            return new int [][] {
                null,
                null,
                null
            };
        }
        return hologramViewLayers;
    }

    public void setHologramViewLayers(int [][] layers) {
        this.hologramViewLayers = layers;
    }
}
