package com.vortexac.listeners;

import com.vortexac.VortexAC;
import com.vortexac.managers.AlertManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerMoveListener implements Listener {
    
    private final VortexAC plugin;
    private final Map<UUID, PlayerData> playerData = new HashMap<>();
    
    public PlayerMoveListener(VortexAC plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.isCancelled()) return;
        
        Player player = event.getPlayer();
        
        if (player.hasPermission("vortex.bypass")) return;
        
        if (!hasPlayedLongEnough(player)) return;
        
        checkSpeed(event);
        checkFly(event);
        checkJesus(event);
    }
    
    private boolean hasPlayedLongEnough(Player player) {
        long playTime = player.getPlayerTime();
        int minTime = plugin.getConfig().getInt("safety.min-playtime-seconds", 30);
        return playTime >= minTime * 20;
    }
    
    private void checkSpeed(PlayerMoveEvent event) {
        if (!plugin.getConfigManager().isCheckEnabled("speed")) return;
        
        Player player = event.getPlayer();
        
        if (player.isInsideVehicle()) return;
        if (player.getAllowFlight()) return;
        if (player.getGameMode() == org.bukkit.GameMode.CREATIVE) return;
        if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR) return;
        
        Location from = event.getFrom();
        Location to = event.getTo();
        
        double horizontalSpeed = calculateHorizontalSpeed(from, to);
        double maxSpeed = plugin.getConfig().getDouble("checks.speed.max-speed", 0.85);
        
        if (horizontalSpeed > maxSpeed && !isOnGround(player) && !isInWater(player)) {
            PlayerData data = getPlayerData(player.getUniqueId());
            data.speedViolations++;
            
            int threshold = plugin.getConfigManager().getViolationThreshold("speed");
            
            if (data.speedViolations >= threshold) {
                AlertManager alertManager = plugin.getAlertManager();
                alertManager.alert(player, "Speed", data.speedViolations);
                
                if (shouldAction("speed")) {
                    kickPlayer(player, "Speed");
                }
            }
        } else {
            getPlayerData(player.getUniqueId()).speedViolations = 
                Math.max(0, getPlayerData(player.getUniqueId()).speedViolations - 1);
        }
    }
    
    private void checkFly(PlayerMoveEvent event) {
        if (!plugin.getConfigManager().isCheckEnabled("fly")) return;
        
        Player player = event.getPlayer();
        
        if (player.getAllowFlight()) return;
        if (player.isInsideVehicle()) return;
        if (player.getGameMode() == org.bukkit.GameMode.CREATIVE) return;
        if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR) return;
        
        Location from = event.getFrom();
        Location to = event.getTo();
        
        double yDiff = to.getY() - from.getY();
        
        if (yDiff > 0.1 && !isOnGround(player) && !isInWater(player) && !isOnScaffolding(player)) {
            PlayerData data = getPlayerData(player.getUniqueId());
            data.flyViolations++;
            
            int threshold = plugin.getConfigManager().getViolationThreshold("fly");
            
            if (data.flyViolations >= threshold) {
                AlertManager alertManager = plugin.getAlertManager();
                alertManager.alert(player, "Fly", data.flyViolations);
                
                if (shouldAction("fly")) {
                    kickPlayer(player, "Fly");
                }
            }
        } else {
            getPlayerData(player.getUniqueId()).flyViolations = 
                Math.max(0, getPlayerData(player.getUniqueId()).flyViolations - 1);
        }
    }
    
    private void checkJesus(PlayerMoveEvent event) {
        if (!plugin.getConfigManager().isCheckEnabled("jesus")) return;
        
        Player player = event.getPlayer();
        
        if (player.getAllowFlight()) return;
        
        Location loc = player.getLocation();
        Material blockBelow = loc.clone().add(0, -1, 0).getBlock().getType();
        
        if (isOnGround(player) && (blockBelow == Material.WATER || blockBelow == Material.LAVA)) {
            PlayerData data = getPlayerData(player.getUniqueId());
            data.jesusViolations++;
            
            int threshold = plugin.getConfigManager().getViolationThreshold("jesus");
            
            if (data.jesusViolations >= threshold) {
                AlertManager alertManager = plugin.getAlertManager();
                alertManager.alert(player, "Jesus", data.jesusViolations);
                
                if (shouldAction("jesus")) {
                    kickPlayer(player, "Jesus");
                }
            }
        }
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
    
    private double calculateHorizontalSpeed(Location from, Location to) {
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }
    
    private boolean isOnGround(Player player) {
        Location loc = player.getLocation();
        return loc.clone().add(0, -0.5, 0).getBlock().getType() != Material.AIR ||
               loc.clone().add(0, -1, 0).getBlock().getType() != Material.AIR;
    }
    
    private boolean isInWater(Player player) {
        Material feet = player.getLocation().getBlock().getType();
        return feet == Material.WATER || feet == Material.LAVA;
    }
    
    private boolean isOnScaffolding(Player player) {
        Material block = player.getLocation().add(0, -1, 0).getBlock().getType();
        return block == Material.SCAFFOLDING || block == Material.VINE;
    }
    
    private PlayerData getPlayerData(UUID uuid) {
        return playerData.computeIfAbsent(uuid, k -> new PlayerData());
    }
    
    private static class PlayerData {
        int speedViolations = 0;
        int flyViolations = 0;
        int jesusViolations = 0;
    }
}
