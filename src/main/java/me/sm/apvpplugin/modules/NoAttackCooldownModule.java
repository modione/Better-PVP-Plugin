package me.sm.apvpplugin.modules;

import me.sm.apvpplugin.base.AbstractModule;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Objects;

public class NoAttackCooldownModule extends AbstractModule {
    public double cooldown = 1024;
    public NoAttackCooldownModule() {
        setAttackCooldown(cooldown);
    }
    public void setAttackCooldown(double cooldown) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
            assert attribute != null;
            attribute.setBaseValue(cooldown);
        }
    }
    @EventHandler
    public void on_Join(PlayerJoinEvent event) {
        Objects.requireNonNull(event.getPlayer().getAttribute(Attribute.GENERIC_ATTACK_SPEED)).setBaseValue(cooldown);
    }

    @Override
    public String getName() {
        return "No Attack CoolDown";
    }
}
