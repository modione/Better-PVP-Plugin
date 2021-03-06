package me.sm.apvpplugin.modules;

import me.sm.apvpplugin.base.AbstractModule;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;;
import org.bukkit.inventory.ItemStack;

public class DisableShieldsModule extends AbstractModule {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if(event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) return;
        ItemStack item = event.getItem();
        if(item == null || item.getType() != Material.SHIELD) return;
        event.setCancelled(true);
        event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(
            ChatColor.RED + "Shields are disabled on this server!"));
    }

    @Override
    public String getName() {
        return "Disable Shields";
    }
}
