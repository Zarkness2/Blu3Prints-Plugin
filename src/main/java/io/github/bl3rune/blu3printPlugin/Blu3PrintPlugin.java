package io.github.bl3rune.blu3printPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.Gson;

import io.github.bl3rune.blu3printPlugin.config.Blu3printConfiguration;
import io.github.bl3rune.blu3printPlugin.data.Blu3printData;
import io.github.bl3rune.blu3printPlugin.data.ImportedBlu3printData;
import io.github.bl3rune.blu3printPlugin.enums.CommandType;
import io.github.bl3rune.blu3printPlugin.items.Blu3printItem;
import io.github.bl3rune.blu3printPlugin.listeners.BookListener;
import io.github.bl3rune.blu3printPlugin.listeners.MenuInteractListener;
import io.github.bl3rune.blu3printPlugin.listeners.PlayerInteractListener;

/** 
 * The main class of the Blu3Print plugin
 * 
 * @author bl3rune
 */
public final class Blu3PrintPlugin extends JavaPlugin {

    private static Blu3PrintPlugin instance;

    public static Blu3PrintPlugin getBlu3PrintPlugin() {
        return instance;
    }
    
    private HashMap<String, Blu3printData> cachedBlueprints = new HashMap<>();
    private Gson gson = new Gson();

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("Starting Blu3Print Plugin");
        instance = this;
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        Blu3printConfiguration.refreshConfiguration();
        addBlu3printRecipes();

        loadSavedBlueprintsToCache();

        getServer().getPluginManager().registerEvents(new PlayerInteractListener(), this);
        getServer().getPluginManager().registerEvents(new MenuInteractListener(), this);
        getServer().getPluginManager().registerEvents(new BookListener(), this);

        for (CommandType commandType : CommandType.values()) {
            getCommand(commandType.getFullCommandName()).setExecutor(commandType.getCommandExecutor());
            if (commandType.getTabCompleter() != null) {
                getCommand(commandType.getFullCommandName()).setTabCompleter(commandType.getTabCompleter());
            }
        }

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("Stopping Blu3Print Plugin");
        saveCachedBlu3prints();
    }

    private void addBlu3printRecipes() {
        
        ShapelessRecipe recipe = new ShapelessRecipe(new NamespacedKey(this, "Blu3print_Writer"), Blu3printItem.getBlankBlu3print());
        recipe.setGroup("Tools & Utilities");
        List<String> ingredients = getConfig().getStringList("blu3print.recipe.ingredients");

        try {
            for (String i : ingredients) {
                recipe.addIngredient(Material.valueOf(i));
            }
            Bukkit.addRecipe(recipe);
        } catch (Exception e) {
            getLogger().warning("Invalid blu3print recipe. using default recipe");
            ShapelessRecipe shapelessRecipe = new ShapelessRecipe(new NamespacedKey(this, "Blu3print_Writer"), Blu3printItem.getBlankBlu3print());
            shapelessRecipe.addIngredient(Material.PAPER);
            shapelessRecipe.addIngredient(Material.LAPIS_LAZULI);
            shapelessRecipe.addIngredient(Material.FEATHER);
            Bukkit.addRecipe(shapelessRecipe);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadSavedBlueprintsToCache() {
        try {
            File file = new File(getDataFolder().getAbsolutePath() + File.separator + "blu3prints.json");
            if (!file.exists()) { 
                return;
            }
            
            FileReader reader = new FileReader(file);
            Map<String, String> entries = gson.fromJson(reader, Map.class);
            if (entries == null || entries.isEmpty()) {
                getLogger().warning("No saved blu3prints found in blu3prints.json");
                return;
            }
            HashMap<String, Blu3printData> map = new HashMap<>();
            for  (Entry<String, String> entry  : entries.entrySet())  {
                Blu3printData data = new ImportedBlu3printData(null, entry.getValue());
                map.put(entry.getKey(), data);
            }
            getLogger().warning("Loaded Cached blu3prints from blu3prints.json");
            map.forEach((k,v) -> getLogger().info(k + " : " + v.getEncodedString()));
            cachedBlueprints = map;

        } catch (Exception e) {
            getLogger().severe("Failed to load blu3prints to cache : " + e.getMessage());
            e.printStackTrace();
        }
    
    }

    public synchronized void saveCachedBlu3prints()  {
        try {
            File file = new File(getDataFolder().getAbsolutePath() + File.separator + "blu3prints.json");
            file.getParentFile().mkdir();
            file.createNewFile();

            FileWriter writer = new FileWriter(file);
            Map<String, String> saveEncodings = new HashMap<>();
            cachedBlueprints.entrySet().forEach(entry -> {
                saveEncodings.put(entry.getKey(), entry.getValue().getEncodedString());
            });
            gson.toJson(saveEncodings, writer);
            writer.flush();
            writer.close();
            getLogger().warning("Cached blu3prints saved to blu3prints.json");
        } catch (Exception e) {
            getLogger().severe("Failed to save cached blu3prints : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Blu3printData getBlu3printFrpmCache(ItemStack blu3print, Player player)   {
        String key = Blu3printItem.extractCacheKeyFromBlu3print(blu3print);
        if (key == null) {
            logger().warning("Blu3print ID is missing from the server cache");
            player.sendMessage("Blu3print ID is missing from the server cache");
        } 
        return getBlu3printFrpmCache(key);
    }

    public Blu3printData getBlu3printFrpmCache(String key)  {
        return cachedBlueprints.getOrDefault(key, null);
    }

    public String getKeyFromEncoding(String encoded) {
        return cachedBlueprints.entrySet().stream().filter(e ->
            e.getValue().getEncodedString().equals(encoded)
        ).map(e -> e.getKey()).findFirst().orElse(null);
    }
    
    public synchronized void saveOrUpdateCachedBlu3print(String key, Blu3printData data)  {
        cachedBlueprints.put(key, data);
        if (Blu3PrintPlugin.getBlu3PrintPlugin() != null) {
            Blu3PrintPlugin.getBlu3PrintPlugin().saveCachedBlu3prints();
        }
    }

    public static Logger logger() {
        return instance.getLogger();
    }

}
