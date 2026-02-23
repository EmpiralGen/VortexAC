package com.vortexac.listeners;

import com.vortexac.VortexAC;
import com.vortexac.managers.AlertManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerInteractListener implements Listener {
    
    private final VortexAC plugin;
    private final Map<UUID, BreakData> breakData = new HashMap<>();
    
    public PlayerInteractListener(VortexAC plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        
        Player player = event.getPlayer();
        
        if (player.hasPermission("vortex.bypass")) return;
        
        if (!hasPlayedLongEnough(player)) return;
        
        checkNuker(event);
    }
    
    private boolean hasPlayedLongEnough(Player player) {
        long playTime = player.getPlayerTime();
        int minTime = plugin.getConfig().getInt("safety.min-playtime-seconds", 30);
        return playTime >= minTime * 20;
    }
    
    private void checkNuker(BlockBreakEvent event) {
        if (!plugin.getConfigManager().isCheckEnabled("nuker")) return;
        
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        
        BreakData data = breakData.computeIfAbsent(uuid, k -> new BreakData());
        
        if (now - data.lastBreakTime < 1000) {
            data.breaksThisSecond++;
        } else {
            data.breaksThisSecond = 1;
            data.lastBreakTime = now;
        }
        
        int maxBlocks = plugin.getConfig().getInt("checks.nuker.max-blocks-per-second", 25);
        
        if (data.breaksThisSecond > maxBlocks) {
            data.nukerViolations++;
            
            int threshold = plugin.getConfigManager().getViolationThreshold("nuker");
            
            if (data.nukerViolations >= threshold) {
                AlertManager alertManager = plugin.getAlertManager();
                alertManager.alert(player, "Nuker (" + data.breaksThisSecond + " blocks/s)", data.nukerViolations);
                
                if (shouldAction("nuker")) {
                    kickPlayer(player, "Nuker");
                }
            }
        } else {
            data.nukerViolations = Math.max(0, data.nukerViolations - 1);
        }
        
        data.lastBreakTime = now;
    }
    
    private boolean shouldAction(String check) {
        String action = plugin.getConfigManager().getAction(check);
        return "KICK".equalsIgnoreCase(action);
    }
    
    private void kickPlayer(Player player, String check) {
        String kickMsg = plugin.getConfig().getString("punishments.kick-message", 
            "§9VortexAC §8» §cYou were flagged for §9%check%§c.\n§7If you believe this is a false positive, appeal at your discord.")
            .replace("%check%", check);
        
        player.kickPlayer(kickMsg);
    }
    
    private static class BreakData {
        long lastBreakTime = 0;
        int breaksThisSecond = 0;
        int nukerViolations = 0;
    }
}
