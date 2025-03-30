package io.github.bl3rune.blu3printPlugin.data;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import io.github.bl3rune.blu3printPlugin.Blu3PrintPlugin;
import io.github.bl3rune.blu3printPlugin.enums.Orientation;
import io.github.bl3rune.blu3printPlugin.enums.Rotation;

public class ImportedBlu3printData extends Blu3printData {

    private Integer maxScale;

    public ImportedBlu3printData(Player player, String encodedString) {
        this.encoded = encodedString;
        String header = encodedString.split(Pattern.quote(HEADER_END))[0]; // encodedString >> HEADER + BODY
        String[] splitHeader = header.split(Pattern.quote(COLUMN_END)); // HEADER >> INGREDIENTS MAP + DIMENSIONS + PERSPECTIVE (OPTIONAL)
        this.ingredientsMap = new HashMap<>();
        for (String ingredientFormula : splitHeader[0].split(Pattern.quote(ROW_END))) {
            String[] formulaSplit = ingredientFormula.split(Pattern.quote(MAPS_TO));
            ingredientsMap.put(formulaSplit[1], formulaSplit[0]);
        }
        
        String [] coords = splitHeader[1].split(Pattern.quote(MODIFIER));
        int xSize = Integer.parseInt(coords[0]);
        int ySize = Integer.parseInt(coords[1]);
        int zSize = Integer.parseInt(coords[2]);
        this.ingredientsCount = new HashMap<>();

        Orientation orientation = Orientation.SOUTH;
        Rotation rotation = Rotation.TOP;
        int scale = 1;
        try {
            String[] splitPerspective = splitHeader[2].split(Pattern.quote(ROW_END));
            orientation = Orientation.getOrientation(splitPerspective[0]);
            rotation = Rotation.fromCode(splitPerspective[1]);
            scale = Integer.parseInt(splitPerspective[2]);
        } catch (Exception e) {
            e.printStackTrace();
            // Well you tried
        }

        if (maxScale == null) {
            try {
                maxScale = Integer.parseInt(Blu3PrintPlugin.getBlu3PrintPlugin().getConfig().getString("blu3print.max-scale"));
            } catch (Exception e) {
                maxScale = null;
            }
        }

        if (player != null && maxScale != null && scale > maxScale) {
            if (!player.hasPermission("blu3print.no-scale-limit")) {
                player.sendMessage(ChatColor.RED + "You do not have permission to increase scale over the max scale limit of " + maxScale + "!");
                return;
            }
        }

        this.position = new ManipulatablePosition(zSize, ySize, xSize, orientation, rotation, scale);

        buildSelectionGrid(encodedString.split(Pattern.quote(HEADER_END))[1]);
        if (scale > 1) {
            final int scaleFinal = scale;
            Map<String, Integer> newCount = new HashMap<>();
            this.ingredientsCount.forEach((k,v) -> newCount.put(k, v  * scaleFinal));
            this.ingredientsCount = newCount;
        }
    }

    private void buildSelectionGrid(String body) {
        this.selectionGrid = new MaterialData[position.getZSize()][position.getYSize()][position.getXSize()];
        String prev = null;
        for (char c : body.toCharArray()) {
            String s = String.valueOf(c);
            if (s.equals(ROW_END) || s.equals(COLUMN_END)) {
                fillRemainingRow(position, prev);
                prev = null;
            } else if (prev == null) {
                prev = s;
            } else if (prev.startsWith(DOUBLE_CHARACTER) && prev.length() < 3) { 
                prev = prev + s;
            } else if (BLU3_ENCODE.contains(s) || s.equals(DOUBLE_CHARACTER)) {
                addToSelection(position, prev);
                prev = s;
            } else { // A NUMBER
                prev = prev + s;
            }
        }
    }

    private void addToSelection(ManipulatablePosition position, String encoded) {
        MaterialData materialData = decodeBlock(encoded);
        for (int i = 0; i < materialData.getCount(); i++)  {
            int[] coords =  position.next();
            this.selectionGrid[coords[0]][coords[1]][coords[2]] = materialData;
        }
        if (materialData.getName() != null) {
            int count = this.ingredientsCount.getOrDefault(materialData.getName(), 0);
            this.ingredientsCount.put(materialData.getName(), count + materialData.getCount());
        }
    }

    private void fillRemainingRow(ManipulatablePosition position, String encoded) {
        MaterialData materialData = decodeBlock(encoded);
        int added = 0;
        while(added == 0 || !position.endOfInnerLoop()) {
            int[] coords =  position.next();
            this.selectionGrid[coords[0]][coords[1]][coords[2]] = materialData;
            added++;
        }
        if (materialData.getName() != null) {
            int count = this.ingredientsCount.getOrDefault(materialData.getName(), 0);
            this.ingredientsCount.put(materialData.getName(), count + added);
        }
    }

    private MaterialData decodeBlock(String encoded)  {
        String code;
        int count;

        if (encoded == null) {
            return new MaterialData(null, Material.AIR, null, 1);
        }

        if (encoded.startsWith(DOUBLE_CHARACTER)) {
            code = encoded.substring(1, 3);
            count = encoded.length() > 3 ? Integer.parseInt(encoded.substring(3)) : 1;
        } else {
            code = encoded.substring(0,1);
            count = encoded.length() > 1 ? Integer.parseInt(encoded.substring(1)) : 1;
        }

        String decoded = ingredientsMap.entrySet().stream()
            .filter(e -> e.getValue().equals(code))
                .map(e -> e.getKey()).findFirst()
                    .orElse(null);

        if (decoded == null) {
            return new MaterialData(null, Material.AIR, null, count);
        }

        BlockFace face = null;
        Material material = null;

        if (decoded.contains(MODIFIER)) {
            String[] split = decoded.split(Pattern.quote(MODIFIER));
            Orientation orientation = Orientation.valueOf(split[0]);
            face = orientation.getBlockFace();
            material = Material.getMaterial(split[1]);
        } else {
            material = Material.getMaterial(decoded);
        }
        return new MaterialData(decoded, material, face, count);
    }

}
