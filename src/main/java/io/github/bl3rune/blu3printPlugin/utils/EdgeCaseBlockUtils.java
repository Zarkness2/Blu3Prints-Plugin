package io.github.bl3rune.blu3printPlugin.utils;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Bed;
import org.bukkit.entity.Player;

import io.github.bl3rune.blu3printPlugin.config.GlobalConfig;
import io.github.bl3rune.blu3printPlugin.data.MaterialData;
import io.github.bl3rune.blu3printPlugin.enums.Orientation;

public class EdgeCaseBlockUtils {

    public static boolean isEdgeCaseBlock(MaterialData materialData) {
        // Check if the block is a specific type or has certain properties
        if (Tag.BEDS.isTagged(materialData.getMaterial())) {
            return true;
        }
        return false;
    }

    public static int getEdgeCaseIngredientCountToAdd(MaterialData materialData) {
        if (Tag.BEDS.isTagged(materialData.getMaterial())) {
            if (materialData.getComplexData().contains("part=foot")) {
                return 0;
            }
        }
        return materialData.getCount();
    }

    public static void handleEdgeCasePlacement(Player player, Location location, MaterialData materialData) {
        // Handle the edge cases
        if (Tag.BEDS.isTagged(materialData.getMaterial())) {
            handleBedPlacement(player, location, materialData);
        }
        return;
    }

    private static void handleBedPlacement(Player player, Location location, MaterialData materialData) {
        if (materialData.getComplexData().contains("part=foot")) {
            return;
        }
        
        Block block = location.getBlock();
        Orientation o = materialData.getFace();

        if (GlobalConfig.getRelativePlacement()) {
            BlockFace playerFacing = Orientation.getCartesianBlockFace(player.getFacing());
            switch (playerFacing) {
                case EAST:
                    o = o.getOpposite().getNextCompass();
                    break;
                case NORTH:
                    o = o.getOpposite();
                    break;
                case WEST:
                    o = o.getNextCompass();
                    break;
                default:
                    break;
            }

        }
        final BlockFace facing = o.getBlockFace();
        for (Bed.Part part : Bed.Part.values()) {
            block.setBlockData(Bukkit.createBlockData(materialData.getMaterial(), (data) -> {
                ((Bed) data).setPart(part);
                ((Bed) data).setFacing(facing);
            }));
            block = block.getRelative(facing.getOppositeFace());
        }
    }

}
