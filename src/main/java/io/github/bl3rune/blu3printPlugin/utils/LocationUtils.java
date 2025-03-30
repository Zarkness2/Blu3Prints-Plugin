package io.github.bl3rune.blu3printPlugin.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import static io.github.bl3rune.blu3printPlugin.data.Blu3printData.MODIFIER;

import java.util.regex.Pattern;

public class LocationUtils {

    /**
     * Translates the positionString to Location
     * 
     * @param positionString location in format world:X:Y:Z
     * @return org.bukkit.Location
     */
    public static Location getCoordsFromPosString(String positionString) {
        String[] pos = positionString.split(Pattern.quote(MODIFIER));
        return new Location(Bukkit.getWorld(pos[0]), Integer.parseInt(pos[1]), Integer.parseInt(pos[2]), Integer.parseInt(pos[3]));
    }

    public static int[] reorderNegativeCoords(int pos1, int pos2) {
        if (pos1 > pos2) {
            return new int[] { pos2, pos1 };
        } else {
            return new int[] { pos1, pos2 };
        }
    }

}
