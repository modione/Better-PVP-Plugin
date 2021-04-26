package me.sm.apvpplugin.modules;

import me.sm.apvpplugin.base.AbstractModule;
import me.sm.apvpplugin.utils.FileConfig;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

public class AttributeOverrideModule extends AbstractModule {
    private double attackDamage = 6.969696969696969696969696;
    private double knockbackResistance = 0.0;
    private double armor = 30.0;

    public AttributeOverrideModule(FileConfig config) {
        if(config.getBoolean("attribute-override-enabled")) {
            attackDamage = config.getDouble("attribute-override.attack-damage");
            knockbackResistance = config.getDouble("attribute-override.knockback-resistance");
            armor = config.getDouble("attribute-override.armor");
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(attackDamage);
            player.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(knockbackResistance);
            player.getAttribute(Attribute.GENERIC_ARMOR).setBaseValue(armor);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(attackDamage);
        player.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(knockbackResistance);
        player.getAttribute(Attribute.GENERIC_ARMOR).setBaseValue(armor);
    }

    @Override
    public String getName() {
        return "Attribute Override";
    }

    @Override
    public boolean isEnabled(FileConfig config) {
        return true; // Is always enabled in order to normalize base values
    }
}
