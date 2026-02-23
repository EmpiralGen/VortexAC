package com.vortexac.commands;

import com.vortexac.VortexAC;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TempMuteCommand implements CommandExecutor {
    
    private final VortexAC plugin;
    
    public TempMuteCommand(VortexAC plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("vortex.tempmute")) {
            sender.sendMessage("§9VortexAC §8» §cYou don't have permission to use this command.");
            return true;
        }
        
        if (args.length < 3) {
            sender.sendMessage("§9VortexAC §8» §cUsage: /tempmute <player> <duration> <reason>");
            sender.sendMessage("§9VortexAC §8» §7Example: /tempmute Notch 1h Spamming");
            return true;
        }
        
        String playerName = args[0];
        String duration = args[1];
        
        StringBuilder reasonBuilder = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            reasonBuilder.append(args[i]).append(" ");
        }
        String reason = reasonBuilder.toString().trim();
        String muter = sender.getName();
        
        Player target = Bukkit.getPlayer(playerName);
        if (target != null) {
            if (target.hasPermission("vortex.mute.exempt")) {
                sender.sendMessage("§9VortexAC §8» §cYou cannot mute that player.");
                return true;
            }
        }
        
        plugin.getPunishManager().tempMutePlayer(playerName, duration, reason, muter);
        
        return true;
    }
}
