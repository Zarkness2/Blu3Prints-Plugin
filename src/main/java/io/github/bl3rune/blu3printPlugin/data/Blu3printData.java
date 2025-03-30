package io.github.bl3rune.blu3printPlugin.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Rotatable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import io.github.bl3rune.blu3printPlugin.Blu3PrintPlugin;
import io.github.bl3rune.blu3printPlugin.enums.Orientation;
import io.github.bl3rune.blu3printPlugin.enums.Rotation;

import static io.github.bl3rune.blu3printPlugin.Blu3PrintPlugin.logger;

public abstract class Blu3printData {

    public static final String COLUMN_END = "|";
    public static final String ROW_END = "-";
    public static final String HEADER_END = "~";
    public static final String DOUBLE_CHARACTER = ".";
    public static final String MAPS_TO = "=";
    public static final String MODIFIER = ":";
    private static Integer maxScale;

    public static final List<String> BLU3_ENCODE = List.of(
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
            "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
            "U", "V", "W", "X", "Y", "Z", "a", "b", "c", "d",
            "e", "f", "g", "h", "i", "j", "k", "l", "m", "n",
            "o", "p", "q", "r", "s", "t", "u", "v", "w", "x",
            "y", "z"
    );

    public static final List<String> materialIgnoreList = List.of(
            Material.END_PORTAL.name(),
            Material.NETHER_PORTAL.name()
    );

    protected MaterialData[][][] selectionGrid;       // [z] [y] [x]
    protected Map<String, Integer> ingredientsCount;  // key: material, value: count
    protected Map<String, String> ingredientsMap;     // key: material, value: encoded
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

    public ManipulatablePosition getPosition()  {
        return position;
    }

    public String getEncodedString() {
        return encoded;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(ChatColor.WHITE).append("Ingredients:").append("\n").append(ChatColor.GRAY);
        ingredientsCount.forEach((k,v) -> sb.append(" - ").append(k).append(" : ").append(v * position.getScalingIngredientsMultiplier()).append("\n"));

        sb.append(ChatColor.WHITE).append("Map:").append("\n").append(ChatColor.GRAY);
        ingredientsMap.forEach((k,v) -> sb.append(" - ").append(k).append(" : ").append(v).append("\n"));

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

    public void placeBlocks(Player player, Location location)  {
        Map<String, Integer> missingBlocks = checkPlayerHasBLocksInInventory(player, false);
        if (!missingBlocks.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Missing these blocks to place the blu3print:");
            missingBlocks.forEach((k, v)  -> player.sendMessage(ChatColor.RED + " - " + k + ": " + v));
            return;
        }

        if (!checkSpaceIsClear(location)) {
            if (player.isSneaking()) {
                player.sendMessage(ChatColor.AQUA + "Placing blu3print despite blocks in the way.");
            } else {
                player.sendMessage(ChatColor.RED + "You can't place the blu3print here. There are blocks in the way.");
                player.sendMessage(ChatColor.RED + "To place the blu3print, sneak while using the blu3print.");
                return;
            }
        }

        checkPlayerHasBLocksInInventory(player, true);
        
        position.resetLoops();
        int [] coords = position.next(true);
        while (coords != null) {
            
            int scale = position.getScale();
            MaterialData data  = selectionGrid[coords[0]/scale][coords[1]/scale][coords[2]/scale];
            if (data == null || data.getMaterial() == null) {
                coords = position.next(true);
                continue;
            }

            int x = location.getBlockX() + coords[2];
            int y = location.getBlockY() + 1 + coords[1];
            int z = location.getBlockZ() + coords[0];

            Location placeLocation = new Location(location.getWorld(), x, y, z);
            Block block = placeLocation.getBlock();
            if  (block !=  null && !isBlockIgnorable(block)) {
                coords = position.next(true);
                continue;
            }
            
            placeBlock(placeLocation, data);
            coords = position.next(true);
        }

    }

    private boolean placeBlock(Location location, MaterialData materialData)  {

        World world = Bukkit.getWorld(location.getWorld().getName());
        if (world == null) {
            
            logger().info("World not found: " + location.getWorld().getName());
            return false;
        }
        world.setType(location, materialData.getMaterial());
        if (materialData.getFace() != null) {
            BlockData blockData = world.getBlockData(location);
            if (blockData instanceof Rotatable rotatable) {
                rotatable.setRotation(materialData.getFace());
                world.setBlockData(location, rotatable);
            }
        }
        return true;
    }
    
    private Map<String, Integer> checkPlayerHasBLocksInInventory(Player player, boolean removeBlocks) {
        if (player.getGameMode() == GameMode.CREATIVE) {
            logger().info("Player is in creative mode, skipping inventory check");
            return new HashMap<>();
        }

        Map<String, Integer> ingCountCopy = new HashMap<>(ingredientsCount);
        Map<Integer, ItemStack> inventoryBlocks = new HashMap<>();
        Map<Integer, ItemStack> storageBlocks = new HashMap<>();
        int inventoryIndex = 0;
        boolean endOfInventory = false;
        Inventory inventory = player.getInventory();

        // Filter out storage blocks to a seperate list
        while (!endOfInventory && inventoryIndex < inventory.getSize()) { 
            try {
                ItemStack itemStack = inventory.getItem(inventoryIndex);
                if (itemStack == null ||itemStack.getAmount() == 0 || itemStack.getType() == Material.AIR)  {
                    inventoryIndex++;
                    continue;
                }
                if (itemStack.getItemMeta() instanceof BlockStateMeta bsm && bsm.getBlockState() instanceof Container) {
                    storageBlocks.put(inventoryIndex, itemStack);
                } else {
                    inventoryBlocks.put(inventoryIndex, itemStack);
                }
                inventoryIndex++;
            } catch (Exception e) {
                endOfInventory = true;
            }
        }

        // Process inventory blocks
        inventoryBlocks.forEach((k,v) -> {
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

        if (ingCountCopy.isEmpty()) return ingCountCopy;

        // process storage blocks
        storageBlocks.forEach((k,v) -> {
            BlockStateMeta bsm = (BlockStateMeta) v.getItemMeta();
            Container container = (Container) bsm.getBlockState();
            Inventory containerInventory = container.getInventory();
            Map<Integer, ItemStack> storageInventoryBlocks = new HashMap<>();
            int storageInventoryIndex = 0;
            boolean endOfStorageInventory = false;
            while (!endOfStorageInventory && storageInventoryIndex < containerInventory.getSize())  { 
                try {
                    ItemStack itemStack = containerInventory.getItem(storageInventoryIndex);
                    if  (itemStack == null || itemStack.getAmount() == 0 || itemStack.getType() == Material.AIR)  {
                        storageInventoryIndex++;
                        continue;
                    }
                    storageInventoryBlocks.put(storageInventoryIndex, itemStack);
                    storageInventoryIndex++;
                } catch (Exception e) {
                    endOfStorageInventory = true;
                }
            }

            storageInventoryBlocks.forEach((ik,iv) -> {
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

    private boolean checkSpaceIsClear(Location location)  {
        World world = Bukkit.getWorld(location.getWorld().getName());
        int x = location.getBlockX();
        int y = location.getBlockY() + 1;
        int z = location.getBlockZ();
        int scale = position.getScale();
        position.resetLoops();
        int [] coords = position.next(true);
        while (coords != null) {
            MaterialData materialData = this.selectionGrid[coords[0]/scale][coords[1]/scale][coords[2]/scale];
            if  (materialData  == null || materialData.getName() ==  null) {
                coords = position.next(true);
                continue;
            }
            Block block = world.getBlockAt(x + coords[2], y + coords[1], z + coords[0]);
            if  (block !=  null && !isBlockIgnorable(block))  {
                return false;
            }
            coords = position.next(true);
         
        }
        return true;
    }

    // EDITING BLU3PRINT SECTION

    public String updateOrientation(Orientation newOrientation) {
        Rotation r = position.getRotation();
        int s = position.getScale();
        this.selectionGrid = position.reorientSelectionGrid(selectionGrid, newOrientation, r);
        int newZ = selectionGrid.length;
        int newY = selectionGrid[0].length;
        int newX = selectionGrid[0][0].length;
        return updateManipulatablePosition(new ManipulatablePosition(newZ,newY,newX,newOrientation,r,s));
    }

    public String updateRotation(Rotation newRotation)  {
        Orientation o = position.getOrientation();
        int s = position.getScale();
        this.selectionGrid = position.reorientSelectionGrid(selectionGrid, o, newRotation);
        int newZ = selectionGrid.length;
        int newY = selectionGrid[0].length;
        int newX = selectionGrid[0][0].length;
        return updateManipulatablePosition(new ManipulatablePosition(newZ,newY,newX,o,newRotation,s));
    }

    public String updateScale(Player player, int newScale)  {
        if (maxScale == null) {
            try {
                maxScale = Integer.parseInt(Blu3PrintPlugin.getBlu3PrintPlugin().getConfig().getString("blu3print.max-scale"));
            } catch (Exception e) {
                maxScale = null;
            }
        }

        if (player != null && maxScale != null && newScale > maxScale) {
            if (!player.hasPermission("blu3print.no-scale-limit")) {
                player.sendMessage(ChatColor.RED + "You do not have permission to increase scale over the max scale limit of " + maxScale + "!");
                return "";
            }
        }

        int scale = newScale / position.getScale();
        if (scale != 1) {
            Map<String, Integer> newCount = new HashMap<>();
            this.ingredientsCount.forEach((k,v) -> newCount.put(k, v  * scale));
            this.ingredientsCount = newCount;
        }
        return updateManipulatablePosition(new ManipulatablePosition(position, newScale));
    }

    private String updateManipulatablePosition(ManipulatablePosition newPosition) {
        
        this.position = newPosition;
        String encodedHeader = this.encoded.split(Pattern.quote(HEADER_END))[0];
        String bodyString = this.encoded.split(Pattern.quote(HEADER_END))[1];
        String header = this.buildHeaderWithPerspective(encodedHeader.split(Pattern.quote(COLUMN_END))[0]);
        this.encoded = header + HEADER_END + bodyString;

        String key = Blu3PrintPlugin.getBlu3PrintPlugin().getKeyFromEncoding(this.encoded);
        if (key == null) {
            key = UUID.randomUUID().toString();
            Blu3PrintPlugin.getBlu3PrintPlugin().saveOrUpdateCachedBlu3print(key, this);
        }
        return key;
    }

    // UTILITY METHODS

    protected boolean isBlockIgnorable(Block block) {
        return block == null || block.isEmpty() || block.isLiquid() || materialIgnoreList.contains(block.getType().name());
    }

    /**
     * Finish building the header with dimensions and perspective. Should look like
     * > INGREDIENTS + COLUMN_END + DIMENSIONS + COLUMN_END + PERSPECTIVE DATA
     * >> DIMENSIONS = X_SIZE + MODIFIER + Y_SIZE + MODIFIER + Z_SIZE
     * >> PERSPECTIVE DATA = ORIENTATION + ROW_END + ROTATION + ROW_END + SCALE
     * @param existingHeader Ingredient list
     * @return Finished header
     */
    protected String buildHeaderWithPerspective(String existingHeader) {
        StringBuilder sb = new StringBuilder();
        sb.append(existingHeader);
        sb.append(COLUMN_END);
        sb.append(position.getXSize()).append(MODIFIER).append(position.getYSize()).append(MODIFIER).append(position.getZSize());
        sb.append(COLUMN_END);
        sb.append(position.getOrientation().getDescription()).append(ROW_END);
        sb.append(position.getRotation().getCode()).append(ROW_END);
        sb.append(position.getScale());
        return sb.toString();
    }

}
