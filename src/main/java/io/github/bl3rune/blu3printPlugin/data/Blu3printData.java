package io.github.bl3rune.blu3printPlugin.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import io.github.bl3rune.blu3printPlugin.config.Blu3printConfiguration;
import io.github.bl3rune.blu3printPlugin.enums.Orientation;
import io.github.bl3rune.blu3printPlugin.enums.Rotation;
import io.github.bl3rune.blu3printPlugin.enums.Turn;
import io.github.bl3rune.blu3printPlugin.utils.EncodingUtils;
import io.github.bl3rune.blu3printPlugin.utils.Pair;

import static io.github.bl3rune.blu3printPlugin.Blu3PrintPlugin.logger;

public abstract class Blu3printData {

    protected static List<String> materialIgnoreList = new ArrayList<>();

    protected MaterialData[][][] selectionGrid; // [z] [y] [x]
    protected Map<String, Integer> ingredientsCount; // key: material, value: count
    protected Map<String, String> ingredientsMap; // key: material, value: encoded
    protected ManipulatablePosition position;
    protected String encoded;

    public MaterialData[][][] getSelectionGrid() {
        return selectionGrid;
    }

    public Map<String, Integer> getIngredientsCount() {
        return ingredientsCount;
    }

    public Map<String, String> getIngredientsMap() {
        return ingredientsMap;
    }

    public ManipulatablePosition getPosition() {
        return position;
    }

    public String getEncodedString() {
        return encoded;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(ChatColor.WHITE).append("Ingredients:").append("\n").append(ChatColor.GRAY);
        ingredientsCount.forEach((k, v) -> sb.append(" - ").append(k).append(" : ")
                .append(v * position.getScalingIngredientsMultiplier()).append("\n"));

        sb.append(ChatColor.WHITE).append("Map:").append("\n").append(ChatColor.GRAY);
        ingredientsMap.forEach((k, v) -> sb.append(" - ").append(k).append(" : ").append(v).append("\n"));

        sb.append(ChatColor.WHITE).append("Position:").append("\n").append(ChatColor.GRAY);
        sb.append(" - X Size :").append(position.getXSize() * position.getScale()).append("\n");
        sb.append(" - Y Size :").append(position.getYSize() * position.getScale()).append("\n");
        sb.append(" - Z Size :").append(position.getZSize() * position.getScale()).append("\n");
        sb.append(" - Orientation :").append(position.getOrientation().name()).append("\n");
        sb.append(" - Rotation :").append(position.getRotation().name()).append("\n");
        sb.append(" - Scale :").append(position.getScale()).append("\n");
        return sb.toString();
    }

    // PLACING BLU3PRINT SECTION

    public void placeBlocks(Player player, Location location) {
        Map<String, Integer> blocksUnableToPlace = checkSpaceIsClear(location);

        Map<String, Integer> missingBlocks = checkPlayerHasBLocksInInventory(player, false, blocksUnableToPlace);
        if (!missingBlocks.isEmpty()) {
            sendMessage(player, ChatColor.RED + "Missing these blocks to place the blu3print:");
            missingBlocks.forEach((k, v) -> sendMessage(player, ChatColor.RED + " - " + k + ": " + v));
            return;
        }

        if (!blocksUnableToPlace.isEmpty()) {
            if (player.isSneaking()) {
                sendMessage(player, ChatColor.AQUA + "Forcing placing blu3print despite blocks in the way.");
            } else {
                sendMessage(player, ChatColor.RED + "You can't place the blu3print here. There are blocks in the way.");
                sendMessage(player, 
                        ChatColor.RED + "To force placement of the blu3print, sneak while using the blu3print.");
                return;
            }
        }

        checkPlayerHasBLocksInInventory(player, true, blocksUnableToPlace);

        int[] coords = position.next(true);
        while (coords != null) {

            int scale = position.getScale();
            MaterialData data = selectionGrid[coords[0] / scale][coords[1] / scale][coords[2] / scale];
            if (data == null || data.getMaterial() == null) {
                coords = position.next(true);
                continue;
            }

            int x = location.getBlockX() + coords[2];
            int y = location.getBlockY() + 1 + coords[1];
            int z = location.getBlockZ() + coords[0];

            Location placeLocation = new Location(location.getWorld(), x, y, z);
            Block block = placeLocation.getBlock();
            if (block != null && !isBlockIgnorable(block)) {
                coords = position.next(true);
                continue;
            }

            placeBlock(placeLocation, data);
            coords = position.next(true);
        }

    }

    private boolean placeBlock(Location location, MaterialData materialData) {

        World world = Bukkit.getWorld(location.getWorld().getName());
        if (world == null) {

            logger().info("World not found: " + location.getWorld().getName());
            return false;
        }

        world.setType(location, materialData.getMaterial());
        if (materialData.getFace() != null) {
            BlockData blockData = world.getBlockData(location);
            if (blockData != null && blockData instanceof Directional) {
                Directional directional = (Directional) blockData;
                try {
                    System.out.println(directional.getAsString());
                    directional.setFacing(materialData.getFace());
                    world.setBlockData(location, directional);
                } catch (Exception e) {
                    // Tried to rotate to stupid direction
                }
            }
        }
        return true;
    }

    private Map<String, Integer> checkPlayerHasBLocksInInventory(Player player, boolean removeBlocks,
            Map<String, Integer> blocksUnableToPlace) {
        if (player.getGameMode() == GameMode.CREATIVE || player.hasPermission("blu3print.no-block-cost")) {
            if (removeBlocks) {
                sendMessage(player, ChatColor.GREEN + "Placing Blu3print for free!");
            }
            return new HashMap<>();
        }

        Map<String, Integer> ingCountCopy = new HashMap<>(ingredientsCount);

        // Discount blocks unable to place
        if (player.isSneaking() && player.hasPermission("blu3print.force-place-discount")) {
            blocksUnableToPlace.forEach((material, amount) -> {
                Integer count = ingCountCopy.getOrDefault(material, 0);
                count = count - amount;
                if (count < 1) {
                    ingCountCopy.remove(material);
                } else {
                    ingCountCopy.put(material, amount);
                }
            });
        }
        Map<Integer, ItemStack> inventoryBlocks = new HashMap<>();
        Map<Integer, ItemStack> storageBlocks = new HashMap<>();
        int inventoryIndex = 0;
        boolean endOfInventory = false;
        Inventory inventory = player.getInventory();

        // Filter out storage blocks to a seperate list
        while (!endOfInventory && inventoryIndex < inventory.getSize()) {
            try {
                ItemStack itemStack = inventory.getItem(inventoryIndex);
                if (itemStack == null || itemStack.getAmount() == 0 || itemStack.getType() == Material.AIR) {
                    inventoryIndex++;
                    continue;
                }
                if (itemStack.getItemMeta() instanceof BlockStateMeta) {
                    BlockStateMeta bsm = (BlockStateMeta) itemStack;
                    if (bsm.getBlockState() instanceof Container) {
                        storageBlocks.put(inventoryIndex, itemStack);
                    } else {
                        inventoryBlocks.put(inventoryIndex, itemStack);
                    }
                } else {
                    inventoryBlocks.put(inventoryIndex, itemStack);
                }
                inventoryIndex++;
            } catch (Exception e) {
                endOfInventory = true;
            }
        }

        // Process inventory blocks
        inventoryBlocks.forEach((k, v) -> {
            String blockName = v.getType().name();
            if (ingCountCopy.containsKey(blockName)) {
                int count = ingCountCopy.get(blockName);
                int stillNeeded = count - v.getAmount();
                if (stillNeeded < 1) {
                    ingCountCopy.remove(blockName);
                    if (removeBlocks) {
                        v.setAmount(v.getAmount() - count);
                        inventory.setItem(k, v);
                    }
                } else {
                    ingCountCopy.put(blockName, count);
                    if (removeBlocks) {
                        inventory.setItem(k, null);
                    }
                }
            }
        });

        if (ingCountCopy.isEmpty())
            return ingCountCopy;

        // process storage blocks
        storageBlocks.forEach((k, v) -> {
            BlockStateMeta bsm = (BlockStateMeta) v.getItemMeta();
            Container container = (Container) bsm.getBlockState();
            Inventory containerInventory = container.getInventory();
            Map<Integer, ItemStack> storageInventoryBlocks = new HashMap<>();
            int storageInventoryIndex = 0;
            boolean endOfStorageInventory = false;
            while (!endOfStorageInventory && storageInventoryIndex < containerInventory.getSize()) {
                try {
                    ItemStack itemStack = containerInventory.getItem(storageInventoryIndex);
                    if (itemStack == null || itemStack.getAmount() == 0 || itemStack.getType() == Material.AIR) {
                        storageInventoryIndex++;
                        continue;
                    }
                    storageInventoryBlocks.put(storageInventoryIndex, itemStack);
                    storageInventoryIndex++;
                } catch (Exception e) {
                    endOfStorageInventory = true;
                }
            }

            storageInventoryBlocks.forEach((ik, iv) -> {
                String blockName = iv.getType().name();
                if (ingCountCopy.containsKey(blockName)) {
                    int count = ingCountCopy.get(blockName);
                    int stillNeeded = count - iv.getAmount();
                    if (stillNeeded < 1) {
                        ingCountCopy.remove(blockName);
                        if (removeBlocks) {
                            iv.setAmount(iv.getAmount() - count);
                            containerInventory.setItem(ik, iv);
                        }
                    } else {
                        ingCountCopy.put(blockName, count);
                        if (removeBlocks) {
                            containerInventory.setItem(ik, null);
                        }
                    }
                }
            });
            bsm.setBlockState(container);
            v.setItemMeta(bsm);
            inventory.setItem(k, v); // update container item in inventory
            if (ingCountCopy.isEmpty()) {
                return;
            }
        });

        return ingCountCopy;
    }

    private Map<String, Integer> checkSpaceIsClear(Location location) {
        World world = Bukkit.getWorld(location.getWorld().getName());
        int x = location.getBlockX();
        int y = location.getBlockY() + 1;
        int z = location.getBlockZ();
        int scale = position.getScale();
        int[] coords = position.next(true);
        Map<String, Integer> blocksUnableToPlace = new HashMap<>();
        while (coords != null) {
            MaterialData materialData = this.selectionGrid[coords[0] / scale][coords[1] / scale][coords[2] / scale];
            if (materialData == null || materialData.getName() == null) {
                coords = position.next(true);
                continue;
            }
            Block block = world.getBlockAt(x + coords[2], y + coords[1], z + coords[0]);
            if (block != null && !isBlockIgnorable(block)) {
                Integer count = blocksUnableToPlace.getOrDefault(materialData.getName(), 0);
                blocksUnableToPlace.put(materialData.getName(), count + 1);
            }
            coords = position.next(true);

        }
        return blocksUnableToPlace;
    }

    // EDITING BLU3PRINT SECTION

    public String updateEncodingWithTurn(Turn turn) {
        int s = position.getScale();
        Pair<Orientation,Rotation> turned = position.calculateTurn(turn);
        int [] newSizes = position.getNewSizes(turned.getA());
        newSizes = position.getNewSizes(turned.getB(), newSizes);
        updateDirectionalEncodings(turned.getA(), turned.getB());
        return updateManipulatablePosition(
            new ManipulatablePosition(newSizes[0], newSizes[1], newSizes[2], turned.getA(), turned.getB(), s));
    }

    public String updateEncodingWithOrientation(Orientation newOrientation) {
        Rotation r = position.getRotation();
        int s = position.getScale();
        int[] newSizes = position.getNewSizes(newOrientation);
        updateDirectionalEncodings(newOrientation, r);
        return updateManipulatablePosition(
                new ManipulatablePosition(newSizes[0], newSizes[1], newSizes[2], newOrientation, r, s));
    }

    public String updateEncodingWithRotation(Rotation newRotation) {
        Orientation o = position.getOrientation();
        int s = position.getScale();
        int[] newSizes = position.getNewSizes(newRotation);
        updateDirectionalEncodings(o, newRotation);
        return updateManipulatablePosition(
                new ManipulatablePosition(newSizes[0], newSizes[1], newSizes[2], o, newRotation, s));
    }

    public String updateEncodingWithScale(Player player, int newScale) {
        Integer maxScale = Blu3printConfiguration.getMaxScale();
        if (player != null && maxScale != null && newScale > maxScale) {
            if (!player.hasPermission("blu3print.no-scale-limit")) {
                sendMessage(player, ChatColor.RED
                        + "You do not have permission to increase scale over the max scale limit of " + maxScale + "!");
                return null;
            }
        }

        Integer maxOverallSize = Blu3printConfiguration.getMaxOverallSize();
        if (player != null && maxOverallSize != null && ((position.getXSize() * newScale) > maxOverallSize ||
                (position.getYSize() * newScale) > maxOverallSize ||
                (position.getZSize() * newScale) > maxOverallSize)) {
            if (!player.hasPermission("blu3print.no-scale-limit") && !player.hasPermission("blu3print.no-size-limit")) {
                sendMessage(player, ChatColor.RED
                        + "You do not have permission to increase size over the max overall size limit of "
                        + maxOverallSize + "!");
                return null;
            }
        }

        int scale = newScale / position.getScale();
        if (scale != 1) {
            Map<String, Integer> newCount = new HashMap<>();
            this.ingredientsCount.forEach((k, v) -> newCount.put(k, v * scale));
            this.ingredientsCount = newCount;
        }
        return updateManipulatablePosition(new ManipulatablePosition(position, newScale));
    }

    private void updateDirectionalEncodings(Orientation o, Rotation r) {
        Map<String, String> ingredientsMapCopy = new HashMap<>(ingredientsMap);
        // DO NOTHING FOR NOW
        // this.ingredientsMap = ingredientsMapCopy;
    }

    private String updateManipulatablePosition(ManipulatablePosition newPosition) {
        String bodyString = EncodingUtils.getBodyFromEncoding(encoded);
        String newHeader = EncodingUtils.buildHeaderWithPerspective(EncodingUtils.ingredientsMapToString(ingredientsMap), newPosition);
        String newEncoding = EncodingUtils.buildEncodedString(newHeader, bodyString);
        System.out.println("Updated blu3print: " + newEncoding);
        return newEncoding;
    }

    // UTILITY METHODS

    protected void sendMessage(Player player, String message) {
        if (player == null) {
            System.out.println(message);
        } else {
            player.sendMessage(message);
        }
    }

    protected boolean isBlockIgnorable(Block block) {
        return block == null || block.isEmpty() || block.isLiquid()
                || materialIgnoreList.contains(block.getType().name());
    }

    protected boolean sizesExceedLimit(int[] sizes, int scale, int max) {
        for (int size : sizes) {
            if (size * scale > max) {
                return true;
            }
        }
        return false;
    }

}
