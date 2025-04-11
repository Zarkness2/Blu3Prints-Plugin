package io.github.bl3rune.blu3printPlugin.data;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import io.github.bl3rune.blu3printPlugin.config.Blu3printConfiguration;
import io.github.bl3rune.blu3printPlugin.utils.EncodingUtils;

import static io.github.bl3rune.blu3printPlugin.utils.EncodingUtils.ROW_END;
import static io.github.bl3rune.blu3printPlugin.utils.EncodingUtils.COLUMN_END;
import static io.github.bl3rune.blu3printPlugin.utils.EncodingUtils.DOUBLE_CHARACTER;
import static io.github.bl3rune.blu3printPlugin.utils.EncodingUtils.MODIFIER;

public class ImportedBlu3printData extends Blu3printData {

    public ImportedBlu3printData(Player player, String encodedString) {

        if (materialIgnoreList.isEmpty()) {
            materialIgnoreList = Blu3printConfiguration.getIgnoredMaterials();
        }

        this.encoded = encodedString;
        String header = EncodingUtils.getHeaderFromEncoding(encodedString);
        this.ingredientsMap = EncodingUtils.getIngredientsMapFromHeader(header);
        if (this.ingredientsMap.isEmpty()) {
            sendMessage(player,"Invalid Blu3print data provided! @ingredients");
            return;
        }
        
        int [] sizes = EncodingUtils.getSizesFromHeader(header);
        if (sizes.length < 3) {
            sendMessage(player,"Invalid Blu3print data provided! @sizes");
            return;
        }
        this.ingredientsCount = new HashMap<>();

        Integer maxSize = Blu3printConfiguration.getMaxSize();
        if (player != null && maxSize != null && sizesExceedLimit(sizes, 1, maxSize)) {
            if (!player.hasPermission("blu3print.no-size-limit")) {
                sendMessage(player,ChatColor.RED + "You do not have permission to set size over the max size limit of " + maxSize + "!");
                return;
            }
        }

        ManipulatablePosition dData = EncodingUtils.getDirectionalDataFromHeader(header);

        Integer maxScale = Blu3printConfiguration.getMaxScale();
        if (player != null && maxScale != null && dData.getScale() > maxScale) {
            if (!player.hasPermission("blu3print.no-scale-limit")) {
                sendMessage(player,ChatColor.RED + "You do not have permission to increase scale over the max scale limit of " + maxScale + "!");
                return;
            }
        }

        Integer maxOverallSize = Blu3printConfiguration.getMaxOverallSize();
        if (player != null && maxOverallSize != null && sizesExceedLimit(sizes, dData.getScale(), maxOverallSize)) {
            if (!player.hasPermission("blu3print.no-scale-limit") && !player.hasPermission("blu3print.no-size-limit")) {
                sendMessage(player,ChatColor.RED + "You do not have permission to increase size over the max overall size limit of " + maxOverallSize + "!");
                return;
            }
        }

        this.position = new ManipulatablePosition(sizes[2], sizes[1], sizes[0], dData.getOrientation(), dData.getRotation(), dData.getScale());

        buildSelectionGrid(EncodingUtils.getBodyFromEncoding(encodedString));
        if (dData.getScale() > 1) {
            final int scaleFinal = dData.getScale();
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
            } else if (EncodingUtils.isEncoded(s) || s.equals(DOUBLE_CHARACTER)) {
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
            int[] coords =  position.next(false);
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
            int[] coords =  position.next(false);
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
            return new ImportedMaterialData(null, Material.AIR, null, 1);
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
            return new ImportedMaterialData(null, Material.AIR, null, count);
        }

        String encodedComplexData = null;
        Material material = null;

        String[] split = EncodingUtils.modifierSplit(decoded);
        if (split.length > 1) {
            if (!split[0].contains("=")) {
                material = Material.matchMaterial(decoded.toLowerCase());
            } else {
                material = Material.matchMaterial(decoded.substring(decoded.indexOf(MODIFIER) + 1));
                encodedComplexData = split[0];
            }
        } else {
            material = Material.matchMaterial(decoded);
        }
        return new ImportedMaterialData(decoded, material, encodedComplexData, count);
    }

}
