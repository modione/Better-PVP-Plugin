package me.sm.apvpplugin.commands;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

public class PvpCommand implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length <= 0) {
            sender.sendMessage(ChatColor.RED + "Please use the command like this: " + ChatColor.YELLOW + "/pvp <on|off|toggle|set> [world]");
            return true;
        }
        World world = args.length == 2 ? Bukkit.getWorld(args[1]) : null;
        if(args[0].equals("on")) {
            if(world == null) {
                Bukkit.getWorlds().forEach(w -> w.setPVP(true));
                sender.sendMessage(ChatColor.GREEN + "Enabled " + ChatColor.AQUA + "PvP in every world!");
            } else {
                world.setPVP(true);
                sender.sendMessage(ChatColor.GREEN + "Enabled " + ChatColor.AQUA + "PvP in world " + ChatColor.YELLOW + world.getName() + ChatColor.AQUA + "!");
            }
        } else if(args[0].equals("off")) {
            if(world == null) {
                Bukkit.getWorlds().forEach(w -> w.setPVP(false));
                sender.sendMessage(ChatColor.RED + "Disabled " + ChatColor.AQUA + "PvP in every world!");
            } else {
                world.setPVP(false);
                sender.sendMessage(ChatColor.RED + "Disabled " + ChatColor.AQUA + "PvP in world " + ChatColor.YELLOW + world.getName() + ChatColor.AQUA + "!");
            }
        } else if(args[0].equals("toggle")) {
            if(world == null) {
                Bukkit.getWorlds().forEach(w -> w.setPVP(!w.getPVP()));
                sender.sendMessage(ChatColor.AQUA + "Toggled PvP in every world!");
            } else {
                world.setPVP(!world.getPVP());
                sender.sendMessage((world.getPVP() ? ChatColor.GREEN + "Enabled " : ChatColor.RED + "Disabled ") + ChatColor.AQUA + "PvP in world " + ChatColor.YELLOW + world.getName() + ChatColor.AQUA + "!");
            }
        } else if(args[0].equals("get")) {
            if(world == null) {
                sender.sendMessage("=== PvP in every World: ===");
                Bukkit.getWorlds().forEach(w -> sender.sendMessage(ChatColor.AQUA + w.getName() + ": " + (w.getPVP() ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled")));
                sender.sendMessage("===========================");
            } else {
                world.setPVP(!world.getPVP());
                sender.sendMessage(ChatColor.AQUA + "The PvP in " + world.getName() + " is currently " + (world.getPVP() ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias,
        String[] args) {
        Stream<String> completions = null;
        if(args.length == 1) completions = Stream.of("on", "off", "toggle", "get");
        else if(args.length == 2) completions = Bukkit.getWorlds().stream().map(World::getName);
        else completions = Stream.empty();
        return completions.filter(hit -> hit.startsWith(args[args.length -1])).collect(Collectors.toList());
    }
}
