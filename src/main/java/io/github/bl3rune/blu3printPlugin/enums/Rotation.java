package io.github.bl3rune.blu3printPlugin.enums;

import java.util.stream.Stream;

public enum Rotation {

    TOP("0"),   // 0°
    RIGHT("1"), // 90°
    BOTTOM("2"),// 180°
    LEFT("3");  // 270°

    private final String code;

    Rotation(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public boolean isVertical()  {
        return this == TOP || this == BOTTOM;
    }

    public boolean isHorizontal()  {
        return this == LEFT || this == RIGHT;
    }

    public Rotation getOpposite()  {
        switch (this) {
            case BOTTOM:
                return TOP;
            case LEFT:
                return RIGHT;
            case RIGHT:
                return LEFT;
            case TOP:
            default:
                return BOTTOM;
        }
    }

    public Rotation getNextRotation() {
        switch (this) {
            case BOTTOM:
                return Rotation.LEFT;
            case LEFT:
                return Rotation.TOP;
            case RIGHT:
                return Rotation.BOTTOM;
            case TOP:
            default:
                return Rotation.RIGHT;
        }
    }

    public static Rotation fromCode(String code) {
        return Stream.of(Rotation.values()).filter(r -> r.getCode().equals(code))
            .findFirst().orElse(Rotation.TOP);
    }
}
