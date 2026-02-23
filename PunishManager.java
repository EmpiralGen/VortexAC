package com.vortexac.managers;

import com.vortexac.VortexAC;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PunishManager {
    
    private final VortexAC plugin;
    private final Map<UUID, Long> mutedPlayers = new ConcurrentHashMap<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    public PunishManager(VortexAC plugin) {
        this.plugin = plugin;
    }
    
    public void banPlayer(String playerName, String reason, String banner) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
        String kickMessage = plugin.getConfig().getString("punishments.kick-message", 
            "§9VortexAC §8» §cYou were banned.\n§7Reason: %reason%\n§7Appeal at your server discord.")
            .replace("%reason%", reason);
        
        Player onlinePlayer = target.getPlayer();
        if (onlinePlayer != null) {
            onlinePlayer.kickPlayer(kickMessage);
        }
        
        Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(
            playerName, 
            reason + "\n§7Banned by: " + banner, 
            null, 
            banner
        );
        
        logPunishment("BAN", playerName, reason, banner, null);
        notifyStaff("§9VortexAC §8» §c" + playerName + " §7was banned by §c" + banner + " §7for §9" + reason + "§7.");
    }
    
    public void tempMutePlayer(String playerName, String duration, String reason, String muter) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
        long durationSeconds = parseDuration(duration);
        
        if (durationSeconds <= 0) {
            return;
        }
        
        long expireTime = System.currentTimeMillis() + (durationSeconds * 1000);
        mutedPlayers.put(target.getUniqueId(), expireTime);
        
        Player onlinePlayer = target.getPlayer();
        if (onlinePlayer != null) {
            onlinePlayer.sendMessage("§9VortexAC §8» §cYou have been muted for §9" + duration + " §cby §7" + muter);
            onlinePlayer.sendMessage("§9VortexAC §8» §cReason: §7" + reason);
        }
        
        logPunishment("TEMPMUTE", playerName, reason, muter, duration);
        notifyStaff("§9VortexAC §8» §c" + playerName + " §7was muted by §c" + muter + " §7for §9" + duration + " §7(§9" + reason + "§7)");
    }
    
    public boolean isMuted(UUID uuid) {
        Long expireTime = mutedPlayers.get(uuid);
        if (expireTime == null) return false;
        
        if (System.currentTimeMillis() > expireTime) {
            mutedPlayers.remove(uuid);
            return false;
        }
        return true;
    }
    
    public long getMuteRemaining(UUID uuid) {
        Long expireTime = mutedPlayers.get(uuid);
        if (expireTime == null) return 0;
        return Math.max(0, expireTime - System.currentTimeMillis());
    }
    
    public void unmutePlayer(UUID uuid) {
        mutedPlayers.remove(uuid);
    }
    
    private long parseDuration(String duration) {
        try {
            duration = duration.toLowerCase().trim();
            long seconds = 0;
            
            if (duration.contains("s")) {
                seconds += Long.parseLong(duration.replaceAll("[^0-9s]", "").replace("s", ""));
            }
            if (duration.contains("m")) {
                seconds += Long.parseLong(duration.replaceAll("[^0-9m]", "").replace("m", "")) * 60;
            }
            if (duration.contains("h")) {
                seconds += Long.parseLong(duration.replaceAll("[^0-9h]", "").replace("h", "")) * 3600;
            }
            if (duration.contains("d")) {
                seconds += Long.parseLong(duration.replaceAll("[^0-9d]", "").replace("d", "")) * 86400;
            }
            
            return seconds;
        } catch (Exception e) {
            return 0;
        }
    }
    
    private void logPunishment(String type, String player, String reason, String staff, String duration) {
        if (!plugin.getConfig().getBoolean("punishments.log-to-file", true)) return;
        
        try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) dataFolder.mkdirs();
            
            File logFile = new File(dataFolder, "punishments.log");
            FileWriter writer = new FileWriter(logFile, true);
            
            String logEntry = String.format("[%s] %s: %s | Reason: %s | Staff: %s",
                dateFormat.format(new Date()), type, player, reason, staff);
            
            if (duration != null) {
                logEntry += " | Duration: " + duration;
            }
            logEntry += "\n";
            
            writer.write(logEntry);
            writer.close();
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to log punishment: " + e.getMessage());
        }
    }
    
    private void notifyStaff(String message) {
        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (staff.hasPermission(plugin.getConfig().getString("staff.alert-permission", "vortex.alerts"))) {
                staff.sendMessage(message);
            }
        }
    }
}
