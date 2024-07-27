package com.basisplaceholders;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BasisPlaceholders extends JavaPlugin implements Listener {

    private File dataFolder;
    private boolean debugMode; // 调试模式开关

    // 用于存储玩家数据
    private final Map<String, FileConfiguration> playerData = new HashMap<>();

    @Override
    public void onEnable() {
        getLogger().warning("");
        getLogger().warning("\u001B[32mBasisPlaceholders 插件已加载\u001B[31m");
        getLogger().warning("\u001B[32m作者: Wheat QQ: 2743063754\u001B[31m");
        getLogger().warning("");
        // 保存默认配置文件（如果不存在则创建）
        saveDefaultConfig();
        // 加载配置文件
        FileConfiguration config = getConfig();
        debugMode = config.getBoolean("debug-mode", false); // 从配置文件读取调试模式开关

        // 创建数据文件夹
        dataFolder = new File(getDataFolder(), "data");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        // 注册事件
        Bukkit.getPluginManager().registerEvents(this, this);

        // 注册 PlaceholderAPI 的占位符
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new BasisPlaceholdersExpansion(this).register();
        } else {
            getLogger().warning("\u001B[31mBasisPlaceholders 插件已卸载\u001B[31m");
            getLogger().warning("\u001B[31m因为没有前置 PlaceholderAPI 置插，版本至少是 2.11.2 以上。\u001B[31m");
            Bukkit.getPluginManager().disablePlugin(this); // 卸载插件
        }

        // 启动定时任务，每分钟保存一次所有在线玩家的数据
        new BukkitRunnable() {
            @Override
            public void run() {
                saveAllPlayerData();
            }
        }.runTaskTimer(this, 0, 1200); // 1200 ticks = 1 minute
    }

    @Override
    public void onDisable() {
        getLogger().warning("\u001B[31mBasisPlaceholders 插件已卸载，用户数据已保存！\u001B[31m");
        saveAllPlayerData(); // 在插件禁用时保存所有玩家数据
    }

    private File getPlayerDataFile(Player player) {
        return new File(dataFolder, player.getUniqueId() + ".yml");
    }

    private void loadPlayerData(Player player) {
        File playerFile = getPlayerDataFile(player);
        if (playerFile.exists()) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
            playerData.put(player.getUniqueId().toString(), config);
            if (debugMode) getLogger().info("已加载 " + player.getName() + " 的数据");
        } else {
            // 如果文件不存在，创建一个新的数据
            FileConfiguration config = new YamlConfiguration();
            config.set("lastOnline", System.currentTimeMillis());
            config.set("offlineDays", 0);
            playerData.put(player.getUniqueId().toString(), config);
            savePlayerData(player); // 保存新创建的数据
        }
    }

    private void savePlayerData(Player player) {
        File playerFile = getPlayerDataFile(player);
        FileConfiguration config = playerData.get(player.getUniqueId().toString());
        if (config != null) {
            long lastOnlineTime = System.currentTimeMillis();
            config.set("lastOnline", lastOnlineTime);
            config.set("offlineDays", calculateOfflineDays(lastOnlineTime, config.getLong("lastOnline")));
            try {
                config.save(playerFile);
                if (debugMode) getLogger().info("已保存 " + player.getName() + " 的数据到 " + playerFile.getName());
            } catch (IOException e) {
                getLogger().severe("无法保存 " + player.getName() + " 的数据！");
                e.printStackTrace();
            }
        }
    }

    private void saveAllPlayerData() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            savePlayerData(player);
        }
    }

    private long calculateOfflineDays(long currentTime, long lastOnlineTime) {
        long difference = currentTime - lastOnlineTime;
        return difference / (1000 * 60 * 60 * 24); // 转换为天数
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        loadPlayerData(player); // 加载玩家数据
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        savePlayerData(player); // 保存玩家数据
        playerData.remove(player.getUniqueId().toString()); // 移除数据引用以便垃圾回收
        if (debugMode) getLogger().info("已移除 " + player.getName() + " 的数据引用");
    }

    public long getLastOnlineTime(Player player) {
        FileConfiguration config = playerData.get(player.getUniqueId().toString());
        return config != null ? config.getLong("lastOnline") : System.currentTimeMillis();
    }

    public long getOfflineDays(Player player) {
        return calculateOfflineDays(System.currentTimeMillis(), getLastOnlineTime(player));
    }
}
