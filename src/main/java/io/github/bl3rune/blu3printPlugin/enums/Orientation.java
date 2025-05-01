package io.github.bl3rune.blu3printPlugin.enums;

import java.util.stream.Stream;

import org.bukkit.block.BlockFace;

/**
 * 3D orientation the camera is facing towards
 * based on org.bukkit.block.BlockFace
 * 
 * WEST = X- Z- Y+ EAST = X+ Z+ Y+
 * DOWN = Y- X+ Z- UP = Y+ X- Z+
 * NORTH = Z- X+ Y+ SOUTH = Z+ X- Y+
 */
public enum Orientation {

    EAST(BlockFace.EAST,12),
    WEST(BlockFace.WEST,4),
    NORTH(BlockFace.NORTH,8),
    SOUTH(BlockFace.SOUTH,0),
    UP(BlockFace.UP,0),
    DOWN(BlockFace.DOWN,0);

    private final BlockFace blockFace;
    private final String description;
    private final String directional;
    private final String directionalShort;
    private final String rotatable;
    private final String rotatableShort;

    Orientation(BlockFace bf, int rotatable) {
        this.blockFace = getCartesianBlockFace(bf);
        this.description = this.blockFace.name().substring(0, 1);
        this.directional = "facing=" + this.blockFace.name().toLowerCase();
        this.directionalShort = "f=" + this.description.toLowerCase();
        this.rotatable = "rotation=" + Integer.toString(rotatable);
        this.rotatableShort = "r=" + Integer.toString(rotatable);
    }

    public BlockFace getBlockFace() {
        return blockFace;
    }

    public String getDescription() {
        return description;
    }

    public String getDirectional() {
        return directional;
    }

    public String getDirectionalShort() {
        return directionalShort;
    }

    public String getRotatable() {
        return rotatable;
    }
     
    public String getRotatableShort() {
        return rotatableShort;
    }

    public Orientation getOpposite() {
        BlockFace blockFace = getCartesianBlockFace(getBlockFace());
        return getOrientation(blockFace.getOppositeFace());
    }

    public boolean isCompass() {
        if (this == UP || this == DOWN) {
            return false;
        }
        return true;
    }

    public Orientation getNextCompass() {
        switch(this) {
            case NORTH:
                return Orientation.EAST;
            case EAST:
                return Orientation.SOUTH;
            case SOUTH:
                return Orientation.WEST;
            case WEST:
                return Orientation.NORTH;
            default:
               return null;
        }
    }

    public Orientation getNextOrientation() {
        switch (this) {
            case DOWN:
                return Orientation.NORTH;
            case EAST:
                return Orientation.SOUTH;
            case NORTH:
                return Orientation.EAST;
            case SOUTH:
            default:
                return Orientation.WEST;
            case UP:
                return Orientation.DOWN;
            case WEST:
                return Orientation.UP;
        }
    }

    public static Orientation findOrientationInComplexDataString(String s) {
        for (Orientation o : Orientation.values()) {
            if (o.getDirectional().equalsIgnoreCase(s) || o.getDirectionalShort().equalsIgnoreCase(s)) {
                return o;
            }
        }
        return null;
    }

    public static Orientation findRotatableInComplexDataString(String s) {
        for (Orientation o : Orientation.values()) {
            if (o.getRotatable().equalsIgnoreCase(s) || o.getRotatableShort().equalsIgnoreCase(s)) {
                return o;
            }
        }
        return null;
    }

    public static Orientation findMultiFacingInComplexDataString(String s) {
        for (Orientation o : Orientation.values()) {
            if (o.isCompass() && s.equalsIgnoreCase(o.name() + "=true")) {
                return o;
            }
        }
        return null;
    }

    public static Orientation getOrientation(String description) {
        return Stream.of(Orientation.values()).filter(o -> o.getDescription().equals(description))
                .findFirst().orElse(SOUTH);
    }

    public static Orientation getOrientation(BlockFace bf) {
        BlockFace blockFace = getCartesianBlockFace(bf);
        return Stream.of(Orientation.values()).filter(o -> o.getBlockFace().equals(blockFace))
                .findFirst().orElse(SOUTH);
    }

    public static BlockFace getCartesianBlockFace(BlockFace bf) {
        switch (bf) {
            case UP:
            case DOWN:
            case EAST:
            case WEST:
            case NORTH:
            case SOUTH:
                return bf;
            case EAST_NORTH_EAST:
            case EAST_SOUTH_EAST:
                return BlockFace.EAST;
            case WEST_NORTH_WEST:
            case WEST_SOUTH_WEST:
                return BlockFace.WEST;
            case NORTH_EAST:
            case NORTH_NORTH_EAST:
            case NORTH_NORTH_WEST:
            case NORTH_WEST:
                return BlockFace.NORTH;
            case SOUTH_EAST:
            case SOUTH_SOUTH_EAST:
            case SOUTH_SOUTH_WEST:
            case SOUTH_WEST:
            case SELF:
            default:
                return BlockFace.SOUTH;
        }
    }

}
