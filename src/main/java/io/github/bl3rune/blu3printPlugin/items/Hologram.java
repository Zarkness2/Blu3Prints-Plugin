package io.github.bl3rune.blu3printPlugin.items;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import io.github.bl3rune.blu3printPlugin.Blu3PrintPlugin;
import io.github.bl3rune.blu3printPlugin.config.Blu3printConfiguration;
import io.github.bl3rune.blu3printPlugin.data.Blu3printData;
import io.github.bl3rune.blu3printPlugin.data.ManipulatablePosition;
import io.github.bl3rune.blu3printPlugin.data.MaterialData;

public class Hologram {

    private Location location;
    private MaterialData [][][] selectionGrid;
    private ManipulatablePosition position;
    private String uuid;
    private List<ArmorStand> holograms; // List to hold the holograms
    private Function<Location,Location> calculateFinalLocationFunction;


    public Hologram(Player player, Location startLocation, Blu3printData data) {
        this.location = new Location(startLocation.getWorld(), startLocation.getX(), startLocation.getY(), startLocation.getZ());
        this.selectionGrid = data.getSelectionGrid().clone();
        this.position = new ManipulatablePosition(data.getPosition(), data.getPosition().getScale());
        this.holograms = new ArrayList<>(); // Initialize the list of holograms
        calculateFinalLocationFunction = data.buildCalculateFinalLocationFunction(player, startLocation, true);
    }

    public String getUUID() {
        return this.uuid;
    }

    public void placeHologram() {
        // Implementation of placing a hologram
        
        int[] coords = position.next(true);
        while (coords != null) {

            int scale = position.getScale();
            MaterialData data = selectionGrid[coords[0] / scale][coords[1] / scale][coords[2] / scale];
            if (data == null || data.getMaterial() == null) {
                coords = position.next(true);
                continue;
            }
            Location loc = calculateFinalLocationFunction.apply(new Location(location.getWorld(), coords[2], coords[1], coords[0]));
            Location placeLocation = new Location(loc.getWorld(), loc.getX() + 0.5, loc.getY() + 0.1, loc.getZ() + 0.5);
            Block block = placeLocation.getBlock();
            if (block != null && !isBlockIgnorable(block)) {
                coords = position.next(true);
                continue;
            }

            buildArmourStand(placeLocation, data);
            coords = position.next(true);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                removeHologram();
            }
        }.runTaskLater(Blu3PrintPlugin.getBlu3PrintPlugin(), 20 * Blu3printConfiguration.getHologramTtl());
    }

    private void buildArmourStand(Location l, MaterialData data) {
        // Implementation of building an armour stand
        ArmorStand armorStand = (ArmorStand) l.getWorld().spawnEntity(l, EntityType.ARMOR_STAND);
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.setCollidable(false);
        armorStand.teleport(new Location(l.getWorld(), l.getX(), l.getY() - 1.25, l.getZ()));

        ItemStack item = new ItemStack(data.getMaterial());
        EntityEquipment equipment = armorStand.getEquipment();
        equipment.setHelmet(item);

        this.holograms.add(armorStand);
    }


    public void removeHologram() {
        this.holograms.forEach(a -> a.remove());
    }

    private boolean isBlockIgnorable(Block block) {
        return block == null || block.isEmpty();
    }
    
}
