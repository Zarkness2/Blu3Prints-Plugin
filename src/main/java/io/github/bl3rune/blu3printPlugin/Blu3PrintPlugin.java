package io.github.bl3rune.blu3printPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
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
import org.bukkit.scheduler.BukkitRunnable;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import io.github.bl3rune.blu3printPlugin.config.Blu3printConfiguration;
import io.github.bl3rune.blu3printPlugin.data.Blu3printData;
import io.github.bl3rune.blu3printPlugin.data.ImportedBlu3printData;
import io.github.bl3rune.blu3printPlugin.enums.CommandType;
import io.github.bl3rune.blu3printPlugin.enums.SemanticLevel;
import io.github.bl3rune.blu3printPlugin.items.Blu3printItem;
import io.github.bl3rune.blu3printPlugin.listeners.BookListener;
import io.github.bl3rune.blu3printPlugin.listeners.MenuInteractListener;
import io.github.bl3rune.blu3printPlugin.listeners.PlayerInteractListener;
import io.github.bl3rune.blu3printPlugin.listeners.PlayerJoinListener;

/**
 * The main class of the Blu3Print plugin
 * 
 * @author bl3rune
 */
public final class Blu3PrintPlugin extends JavaPlugin {

    private static Blu3PrintPlugin instance;
    private static List<String> updateMessages = new ArrayList<>();

    public static Blu3PrintPlugin getBlu3PrintPlugin() {
        return instance;
    }

    public static List<String> getUpdateMessages() {
        return updateMessages;
    }

    private HashMap<String, Blu3printData> cachedBlueprints = new HashMap<>();
    private Gson gson = new Gson();

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("Starting Blu3Print Plugin");
        checkUpdate();
        instance = this;
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        Blu3printConfiguration.refreshConfiguration();
        addBlu3printRecipes();

        loadSavedBlueprintsToCache();

        getServer().getPluginManager().registerEvents(new PlayerInteractListener(), this);
        getServer().getPluginManager().registerEvents(new MenuInteractListener(), this);
        getServer().getPluginManager().registerEvents(new BookListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);

        for (CommandType commandType : CommandType.values()) {
            getCommand(commandType.getFullCommandName()).setExecutor(commandType.getCommandExecutor());
            if (commandType.getTabCompleter() != null) {
                getCommand(commandType.getFullCommandName()).setTabCompleter(commandType.getTabCompleter());
            }
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                checkUpdate();
            }
        }.runTaskTimer(this, 0, (60 * 60 * 20 * Blu3printConfiguration.getUpdateCheckInterval())); // Run every X hours

    }

    public void checkUpdate() {
        try {
            SemanticLevel semanticLevel = Blu3printConfiguration.getUpdateLoggingLevel();
            if (semanticLevel == SemanticLevel.NONE) {
                return;
            }

            HttpURLConnection con = (HttpURLConnection) new URL(
                    "https://api.github.com/repos/bl3rune/Blu3Prints-Plugin/releases/latest").openConnection();
            con.setDoOutput(true);
            con.setRequestMethod("GET");
            JsonReader reader = gson.newJsonReader(new InputStreamReader(con.getInputStream()));
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            String latestVersion = json.get("tag_name").getAsString();
            String version = this.getDescription().getVersion();
            String[] lv = latestVersion.split("\\.");
            String[] v = version.split("\\.");
            boolean changed = lv.length != v.length;
            switch (semanticLevel) {
                case PATCH:
                    changed = changed || (!lv[2].equals(v[2]));
                case MINOR:
                    changed = changed || (!lv[1].equals(v[1]));
                case MAJOR:
                    changed = changed || (!lv[0].equals(v[0]));
                default:
            }
            if (changed) {
                updateMessages = new ArrayList<>();
                updateMessages.add("§9[Blu3Print]§r§6 New Update Available!");
                updateMessages.add("§9[Blu3Print]§r Current v" + version + " >> Latest v" + latestVersion);
                updateMessages.addAll(getUpdateMessageDetails());
                updateMessages.forEach(um -> Bukkit.getConsoleSender().sendMessage(um));
            }
        } catch (Exception ex) {
            Bukkit.getConsoleSender().sendMessage("§cCould not check for Updates!");
        }
    }

    public List<String> getUpdateMessageDetails() {
        SemanticLevel semanticLevel = Blu3printConfiguration.getUpdateLoggingLevel();
        String version = this.getDescription().getVersion();
        String[] v = version.split("\\.");
        List<String> updateMessages = new ArrayList<>();
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(
                    "https://api.github.com/repos/bl3rune/Blu3Prints-Plugin/releases").openConnection();
            con.setDoOutput(true);
            con.setRequestMethod("GET");
            JsonReader reader = gson.newJsonReader(new InputStreamReader(con.getInputStream()));
            JsonArray jsonArray = JsonParser.parseReader(reader).getAsJsonArray();
            for (JsonElement element : jsonArray.asList()) {
                JsonObject release = element.getAsJsonObject();
                String releaseVersion = release.get("tag_name").getAsString();
                String[] rv = releaseVersion.split("\\.");
                boolean changed = rv.length != v.length;
                switch (semanticLevel) {
                    case PATCH:
                        changed = changed || (!rv[2].equals(v[2]));
                    case MINOR:
                        changed = changed || (!rv[1].equals(v[1]));
                    case MAJOR:
                        changed = changed || (!rv[0].equals(v[0]));
                    default:
                }
                if (!changed) {
                    break;
                }
                String update = release.get("name").getAsString();
                updateMessages.add("§9[Blu3Print]§r " + update);
            }
        } catch (Exception ex) {
            Bukkit.getConsoleSender().sendMessage("§cCould not check for detailed updates!");
        }

        return updateMessages;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("Stopping Blu3Print Plugin");
        saveCachedBlu3prints();
    }

    private void addBlu3printRecipes() {

        ShapelessRecipe recipe = new ShapelessRecipe(new NamespacedKey(this, "Blu3print_Writer"),
                Blu3printItem.getBlankBlu3print());
        recipe.setGroup("Tools & Utilities");
        List<String> ingredients = getConfig().getStringList("blu3print.recipe.ingredients");

        try {
            for (String i : ingredients) {
                recipe.addIngredient(Material.valueOf(i));
            }
            Bukkit.addRecipe(recipe);
        } catch (Exception e) {
            getLogger().warning("Invalid blu3print recipe. using default recipe");
            ShapelessRecipe shapelessRecipe = new ShapelessRecipe(new NamespacedKey(this, "Blu3print_Writer"),
                    Blu3printItem.getBlankBlu3print());
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
            for (Entry<String, String> entry : entries.entrySet()) {
                Blu3printData data = new ImportedBlu3printData(null, entry.getValue());
                map.put(entry.getKey(), data);
            }
            getLogger().warning("Loaded Cached blu3prints from blu3prints.json");
            if (Blu3printConfiguration.isImportedBlu3printsLoggingEnabled()) {
                map.forEach((k, v) -> getLogger().info(k + " : " + v.getEncodedString()));
            }
            cachedBlueprints = map;

        } catch (Exception e) {
            getLogger().severe("Failed to load blu3prints to cache : " + e.getMessage());
            e.printStackTrace();
        }

    }

    public synchronized void saveCachedBlu3prints() {
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

    public Blu3printData getBlu3printFrpmCache(ItemStack blu3print, Player player) {
        String key = Blu3printItem.extractCacheKeyFromBlu3print(blu3print);
        if (key == null) {
            logger().warning("Blu3print ID is missing from the server cache");
            player.sendMessage("Blu3print ID is missing from the server cache");
        }
        return getBlu3printFrpmCache(key);
    }

    public Blu3printData getBlu3printFrpmCache(String key) {
        return cachedBlueprints.getOrDefault(key, null);
    }

    public String getKeyFromEncoding(String encoded) {
        return cachedBlueprints.entrySet().stream().filter(e -> e.getValue().getEncodedString().equals(encoded))
                .map(e -> e.getKey()).findFirst().orElse(null);
    }

    public synchronized void saveOrUpdateCachedBlu3print(String key, Blu3printData data) {
        cachedBlueprints.put(key, data);
        if (Blu3PrintPlugin.getBlu3PrintPlugin() != null) {
            Blu3PrintPlugin.getBlu3PrintPlugin().saveCachedBlu3prints();
        }
    }

    public static Logger logger() {
        return instance.getLogger();
    }

}
