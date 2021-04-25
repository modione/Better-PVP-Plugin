package me.sm.apvpplugin.modules;

import me.sm.apvpplugin.ApvpPlugin;
import me.sm.apvpplugin.base.AbstractModule;
import me.sm.apvpplugin.utils.FileConfig;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;

public class BetterDeathModule extends AbstractModule {
    private Sound soundOther;
    private Sound soundSelf;
    private String title;
    private String subtitle;
    private long timeout;
    private long protectionTime;

    public BetterDeathModule(FileConfig config) {
        soundOther = Sound.valueOf(config.getString("better-death.sound-other"));
        soundSelf = Sound.valueOf(config.getString("better-death.sound-self"));
        title = config.getString("better-death.title");
        subtitle = config.getString("better-death.subtitle");
        timeout = config.getLong("better-death.timeout");
        protectionTime = config.getLong("better-death.protection-time");
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if(event.getEntityType() != EntityType.PLAYER) return;
        Player player = (Player) event.getEntity();
        if(event.getFinalDamage() < player.getHealth()) return;
        Boolean keepInventory = event.getEntity().getWorld().getGameRuleValue(GameRule.KEEP_INVENTORY);
        if(keepInventory != null && keepInventory) player.getInventory().clear();
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        Bukkit.broadcastMessage(String.format("%s died...", player.getName())); // TODO implement custom death messages
        event.setCancelled(true);
        player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
        player.setFoodLevel(20);
        player.sendTitle(title, subtitle, 10, 20, 10);
        Bukkit.getOnlinePlayers().stream().filter(p -> !p.equals(player)).forEach(p -> p.playSound(player.getLocation(), soundOther, SoundCategory.PLAYERS,1.0f, 1.0f));
        Location spawn = player.getBedSpawnLocation() == null ? player.getWorld().getSpawnLocation() : player.getBedSpawnLocation();
        if(timeout <= 0) {
            player.teleport(spawn);
            if(!player.isInvulnerable()) {
                player.setInvulnerable(true);
                Bukkit.getScheduler().runTaskLater(ApvpPlugin.instance, () -> {
                    if(!player.isOnline()) return;
                    player.setInvulnerable(false);
                }, protectionTime * 20L);
            }
        } else {
            GameMode before = player.getGameMode();
            player.setGameMode(GameMode.SPECTATOR);
            Bukkit.getScheduler().runTaskLater(ApvpPlugin.instance, () -> {
                if(!player.isOnline()) return;
                player.teleport(spawn);
                player.setGameMode(before);
                if(!player.isInvulnerable()) {
                    player.setInvulnerable(true);
                    Bukkit.getScheduler().runTaskLater(ApvpPlugin.instance, () -> {
                        if(!player.isOnline()) return;
                        player.setInvulnerable(false);
                    }, protectionTime * 20L);
                }
            }, timeout * 20L);
        }
        player.playSound(player.getLocation(), soundSelf, SoundCategory.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public String getName() {
        return "Better Death";
    }
}
