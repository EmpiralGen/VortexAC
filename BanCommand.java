package com.vortexac.commands;

import com.vortexac.VortexAC;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BanCommand implements CommandExecutor {
    
    private final VortexAC plugin;
    
    public BanCommand(VortexAC plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("vortex.ban")) {
            sender.sendMessage("§9VortexAC §8» §cYou don't have permission to use this command.");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage("§9VortexAC §8» §cUsage: /ban <player> <reason>");
            return true;
        }
        
        String playerName = args[0];
        StringBuilder reasonBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            reasonBuilder.append(args[i]).append(" ");
        }
        String reason = reasonBuilder.toString().trim();
        String banner = sender.getName();
        
        if (Bukkit.getPlayer(playerName) != null) {
            Player target = Bukkit.getPlayer(playerName);
            if (target.hasPermission("vortex.ban.exempt")) {
                sender.sendMessage("§9VortexAC §8» §cYou cannot ban that player.");
                return true;
            }
        }
        
        plugin.getPunishManager().banPlayer(playerName, reason, banner);
        
        return true;
    }
}
