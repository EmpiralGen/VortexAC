package com.vortexac;

import org.bukkit.plugin.java.JavaPlugin;
import com.vortexac.listeners.PlayerMoveListener;
import com.vortexac.listeners.PlayerInteractListener;
import com.vortexac.commands.VortexCommand;
import com.vortexac.commands.BanCommand;
import com.vortexac.commands.TempMuteCommand;
import com.vortexac.managers.AlertManager;
import com.vortexac.managers.PunishManager;
import com.vortexac.utils.ConfigManager;

public class VortexAC extends JavaPlugin {
    
    private static VortexAC instance;
    private AlertManager alertManager;
    private PunishManager punishManager;
    private ConfigManager configManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        saveDefaultConfig();
        configManager = new ConfigManager(this);
        alertManager = new AlertManager(this);
        punishManager = new PunishManager(this);
        
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);
        
        registerCommands();
        
        getLogger().info("§9VortexAC §7- §aEnabled successfully!");
    }
    
    @Override
    public void onDisable() {
        getLogger().info("§9VortexAC §7- §cDisabled!");
    }
    
    private void registerCommands() {
        getCommand("vortex").setExecutor(new VortexCommand(this));
        getCommand("ban").setExecutor(new BanCommand(this));
        getCommand("tempmute").setExecutor(new TempMuteCommand(this));
    }
    
    public static VortexAC getInstance() {
        return instance;
    }
    
    public AlertManager getAlertManager() {
        return alertManager;
    }
    
    public PunishManager getPunishManager() {
        return punishManager;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
}
