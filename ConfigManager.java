package com.vortexac.utils;

import com.vortexac.VortexAC;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    
    private final VortexAC plugin;
    
    public ConfigManager(VortexAC plugin) {
        this.plugin = plugin;
    }
    
    public FileConfiguration getConfig() {
        return plugin.getConfig();
    }
    
    public void reloadConfig() {
        plugin.reloadConfig();
    }
    
    public boolean isCheckEnabled(String check) {
        return getConfig().getBoolean("checks." + check + ".enabled", true);
    }
    
    public int getViolationThreshold(String check) {
        return getConfig().getInt("checks." + check + ".violation-threshold", 5);
    }
    
    public String getAction(String check) {
        return getConfig().getString("checks." + check + ".action", "KICK");
    }
    
    public boolean hasBypassPermission(String permission, org.bukkit.entity.Player player) {
        return player.hasPermission(getConfig().getString("safety.bypass-permission", "vortex.bypass"));
    }
    
    public boolean isSafetyEnabled(String safetySetting) {
        return getConfig().getBoolean("safety." + safetySetting, false);
    }
}
