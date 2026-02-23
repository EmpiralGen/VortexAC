package com.vortexac.commands;

import com.vortexac.VortexAC;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VortexCommand implements CommandExecutor {
    
    private final VortexAC plugin;
    
    public VortexCommand(VortexAC plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("vortex.admin")) {
            sender.sendMessage("§9VortexAC §8» §cYou don't have permission to use this command.");
            return true;
        }
        
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "reload":
                plugin.reloadConfig();
                sender.sendMessage("§9VortexAC §8» §aConfiguration reloaded!");
                break;
                
            case "stats":
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    int vl = plugin.getAlertManager().getTotalViolations(player.getUniqueId());
                    sender.sendMessage("§9VortexAC §8» §7Your violations: §c" + vl);
                } else {
                    sender.sendMessage("§9VortexAC §8» §cThis command is for players only.");
                }
                break;
                
            case "checks":
                sender.sendMessage("§9VortexAC §8» §7Enabled Checks:");
                sender.sendMessage("§9• §7Speed, Fly, Jesus, Nuker, Reach, Velocity");
                break;
                
            case "help":
                sendHelp(sender);
                break;
                
            default:
                sendHelp(sender);
                break;
        }
        
        return true;
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§9§lVortexAC §7- §9Anti-Cheat System");
        sender.sendMessage("§9/vortex reload §7- Reload config");
        sender.sendMessage("§9/vortex stats §7- Check your violations");
        sender.sendMessage("§9/vortex checks §7- List enabled checks");
        sender.sendMessage("§9/ban <player> <reason> §7- Ban a player");
        sender.sendMessage("§9/tempmute <player> <time> <reason> §7- Mute a player temporarily");
    }
}
