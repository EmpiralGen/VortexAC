package com.vortexac.managers;

import com.vortexac.VortexAC;
import org.bukkit.entity.Player;
import org.bukkit.Sound;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AlertManager {
    
    private final VortexAC plugin;
    private final Map<UUID, ViolationData> violations = new HashMap<>();
    
    public AlertManager(VortexAC plugin) {
        this.plugin = plugin;
    }
    
    public void alert(Player player, String check, int vl) {
        if (!plugin.getConfig().getBoolean("alerts.enabled", true)) return;
        
        String format = plugin.getConfig().getString("alerts.alert-format", 
            "§9VortexAC §8» §7%player% §cfailed §9%check% §7(%vl% vl)");
        
        String message = format
            .replace("%player%", player.getName())
            .replace("%check%", check)
            .replace("%vl%", String.valueOf(vl));
        
        for (Player staff : plugin.getServer().getOnlinePlayers()) {
            if (staff.hasPermission(plugin.getConfig().getString("staff.alert-permission", "vortex.alerts"))) {
                staff.sendMessage(message);
                
                if (plugin.getConfig().getBoolean("alerts.sound-enabled", true)) {
                    try {
                        staff.playSound(staff.getLocation(), Sound.valueOf("ENTITY_EXPERIENCE_ORB_PICKUP"), 1f, 1f);
                    } catch (Exception ignored) {}
                }
            }
        }
        
        ViolationData data = violations.computeIfAbsent(player.getUniqueId(), k -> new ViolationData());
        data.addViolation(check);
    }
    
    public int getTotalViolations(UUID uuid) {
        ViolationData data = violations.get(uuid);
        return data != null ? data.getTotal() : 0;
    }
    
    public int getCheckViolations(UUID uuid, String check) {
        ViolationData data = violations.get(uuid);
        return data != null ? data.getCheckViolations(check) : 0;
    }
    
    public void resetViolations(UUID uuid) {
        violations.remove(uuid);
    }
    
    public void broadcastToOps(String message) {
        for (Player staff : plugin.getServer().getOnlinePlayers()) {
            if (staff.hasPermission(plugin.getConfig().getString("staff.alert-permission", "vortex.alerts"))) {
                staff.sendMessage(message);
            }
        }
    }
    
    private static class ViolationData {
        private final Map<String, Integer> checkViolations = new HashMap<>();
        private int total = 0;
        
        public void addViolation(String check) {
            checkViolations.merge(check, 1, Integer::sum);
            total++;
        }
        
        public int getCheckViolations(String check) {
            return checkViolations.getOrDefault(check, 0);
        }
        
        public int getTotal() {
            return total;
        }
    }
}
