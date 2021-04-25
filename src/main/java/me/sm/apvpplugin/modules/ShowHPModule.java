package me.sm.apvpplugin.modules;

import me.sm.apvpplugin.base.AbstractModule;
import org.bukkit.ChatColor;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class ShowHPModule extends AbstractModule {
    @EventHandler
    public void on_BowHit(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (!(event.getDamager() instanceof Arrow)) return;
        if (event.getEntity().isDead()) return;
        Arrow arrow = (Arrow) event.getDamager();
        if (!(arrow.getShooter() instanceof Player)) return;
        Player player = (Player) arrow.getShooter();
        LivingEntity entity = (LivingEntity) event.getEntity();
        double health = entity.getHealth()-event.getDamage();
        if (health<=0) return;
        if (player==entity) return;
        player.sendMessage(entity.getName()+ChatColor.AQUA+" is on " +ChatColor.RED+((int)health)+ChatColor.AQUA+" hp");
    }
    @Override
    public String getName() {
        return "ShowHPonRangedHit";
    }
}
