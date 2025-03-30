package io.github.bl3rune.blu3printPlugin.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;

import io.github.bl3rune.blu3printPlugin.enums.Orientation;
import io.github.bl3rune.blu3printPlugin.enums.Rotation;
import io.github.bl3rune.blu3printPlugin.utils.LocationUtils;

public class CapturedBlu3printData extends Blu3printData {

    public CapturedBlu3printData(Player player, String pos1, String pos2) {
        String world = pos1.split(Pattern.quote(MODIFIER))[0];

        Location loc1 = LocationUtils.getCoordsFromPosString(pos1);
        Location loc2 = LocationUtils.getCoordsFromPosString(pos2);

        int[] locX = LocationUtils.reorderNegativeCoords(loc1.getBlockX(), loc2.getBlockX());
        int[] locY = LocationUtils.reorderNegativeCoords(loc1.getBlockY(), loc2.getBlockY());
        int[] locZ = LocationUtils.reorderNegativeCoords(loc1.getBlockZ(), loc2.getBlockZ());

        int xSize = locX[1] - locX[0] + 1;
        int ySize = locY[1] - locY[0] + 1;
        int zSize = locZ[1] - locZ[0] + 1;

        selectionGrid = new MaterialData[zSize][ySize][xSize];
        this.ingredientsCount = new HashMap<>();
        this.ingredientsMap = new HashMap<>();
        Map<String, Integer> ingredientsCountWithDirection = new HashMap<>();

        for (int z = 0; z < zSize; z++) {
            for (int y = 0; y < ySize; y++) {
                for (int x = 0; x < xSize; x++) {
                    Block block = Bukkit.getWorld(world).getBlockAt(locX[0] + x, locY[0] + y, locZ[0] + z);
                    if (isBlockIgnorable(block)) {
                        selectionGrid[z][y][x] = new MaterialData(null, Material.AIR, null, 1);
                    } else {
                        BlockFace face = null;
                        String blockName = block.getType().name();
                        if (block instanceof Directional directional) {
                            Orientation orientation = Orientation.getOrientation(directional.getFacing());
                            face = orientation.getBlockFace();
                            blockName = orientation.getDescription() + MODIFIER + blockName;
                        }
                        ingredientsCount.put(block.getType().name(), ingredientsCount.getOrDefault(block.getType().name(), 0) + 1);
                        ingredientsCountWithDirection.put(blockName, ingredientsCount.getOrDefault(blockName, 0) + 1);
                        MaterialData materialData = new MaterialData(blockName, block.getType(), face, 1);
                        selectionGrid[z][y][x] = materialData;
                    }
                }
            }
        }

        String header = buildHeaderIngredients(ingredientsCountWithDirection);

        List<ManipulatablePosition> positions = new ArrayList<>();
        for (Orientation o : Orientation.values()) {
            positions.add(new ManipulatablePosition(zSize, ySize, xSize, o, Rotation.TOP));
            positions.add(new ManipulatablePosition(zSize, ySize, xSize, o, Rotation.RIGHT));
            positions.add(new ManipulatablePosition(zSize, ySize, xSize, o, Rotation.BOTTOM));
            positions.add(new ManipulatablePosition(zSize, ySize, xSize, o, Rotation.LEFT));
        }
        
        positions = positions.parallelStream().map(p -> do3dLoop(p)).sorted(
            (p1,p2) -> Integer.compare(p1.getEncoding().length(), p2.getEncoding().length())
        ).toList();

        this.position = positions.get(0);
        header = buildHeaderWithPerspective(header);
        
        this.encoded = header + HEADER_END + positions.get(0).getEncoding();
    }

    private String buildHeaderIngredients(Map<String, Integer> ingredientsCountWithDirection) {
        StringBuilder sb = new StringBuilder();
        ingredientsMap = new HashMap<>();
        int index = 1;
        int secondCharIndex = -1;
        // Sorted so most common keys are lower
        List<String> sortedKeys = ingredientsCountWithDirection.entrySet().stream().sorted(
            (o1, o2) -> o2.getValue().compareTo(o1.getValue())
        ).map(i -> i.getKey()).toList();

        for (String i : sortedKeys) {
            if (index > 1 || secondCharIndex >= 0) {
                sb.append(ROW_END);
            } 
            if (index == BLU3_ENCODE.size()) {
                secondCharIndex++;
                index = 0;
            }
            String code = secondCharIndex < 0 ? BLU3_ENCODE.get(index)
                    : DOUBLE_CHARACTER + BLU3_ENCODE.get(secondCharIndex) + BLU3_ENCODE.get(index);
            ingredientsMap.put(i, code);
            sb.append(code);
            sb.append(MAPS_TO);
            sb.append(i);
            index++;
        }
        return sb.toString();
    }

    private ManipulatablePosition do3dLoop(ManipulatablePosition position) {
        StringBuilder sb = new StringBuilder();
        String previous = null;
        int count = 0;
        boolean empty = true;
        int [] coords = position.next();
        while(coords !=  null) {
            MaterialData materialData = selectionGrid[coords[0]][coords[1]][coords[2]];
            if (materialData == null) {
                materialData = new MaterialData(null, Material.AIR, null, 1);
            }
            String name = materialData.getName();
            if  (materialData.getFace() != null)  {
                Orientation orientation = Orientation.getOrientation(materialData.getFace());
                name = orientation.getDescription() + MODIFIER + name;
            }
            sb.append(determineEncoding(previous, name, count));
            if ((previous == null && name == null) || (name != null && name.equals(previous))) {
                count++;
             } else {
                empty = false;
                previous = name;
                count = 1;
             }

             if (position.endOfInnerLoop()) {
                // ON EMPTY ROW should look like : ROW_END + ROW_END ( or COLUMN_END )
                // OTHERWISE : ... + LAST_MAT (no number required) + ROW_END ( or COLUMN_END )
                sb.append(empty ? "" : determineEncoding(previous, "", 1));
                previous = null;
                count = 0;
                sb.append(position.endOfMiddleLoop() ? COLUMN_END : ROW_END);
                empty = true;
            }
            coords = position.next();
        }
        
        position.setEncoding(sb.toString());
        return position;
    }

    /**
     * Translates structures using the ingredientsMap into an encoded text string
     * 
     * on count zero : no change so return blank string
     * on prev null and current not null: record the null records as AIR (with a count if more than once)
     * on prev not null and current is different : record the previous material  (with a count if more than once)
     * otherwise : return blank string
     * 
     * @param prev    Previous material name
     * @param current Current material name
     * @param count   How many times the previous block type has appeared in a row
     * @return encodedString describing material change or blank string if no change
     */
    private String determineEncoding(String prev, String current, int count) {
        if (count == 0) return "";
        if (prev == null) {
            if (current != null) return BLU3_ENCODE.get(0) + (count < 2 ? "" : count);
        } else if (!prev.equals(current)) {
            return ingredientsMap.getOrDefault(prev, BLU3_ENCODE.get(0)) + (count < 2 ? "" : count);
        }
        return "";
    }

}
