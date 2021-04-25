package me.sm.apvpplugin.modules;

import java.util.List;
import java.util.Map;
import java.util.Random;
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
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerMoveEvent;

public class BetterDeathModule extends AbstractModule {
    private final Sound soundOther;
    private final Sound soundSelf;
    private final String title;
    private final String subtitle;
    private final long timeout;
    private final long protectionTime;
    private final List<String> pvpMessages;
    private final List<String> genericMessages;
    private final Random rng = new Random();

    public BetterDeathModule(FileConfig config) {
        soundOther = Sound.valueOf(config.getString("better-death.sound-other"));
        soundSelf = Sound.valueOf(config.getString("better-death.sound-self"));
        title = config.getString("better-death.title");
        subtitle = config.getString("better-death.subtitle");
        timeout = config.getLong("better-death.timeout");
        protectionTime = config.getLong("better-death.protection-time");
        pvpMessages = config.getStringList("custom-deathmsgs.pvp");
        genericMessages = config.getStringList("custom-deathmsgs.generic");
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if(event.getEntityType() != EntityType.PLAYER) return;
        Player player = (Player) event.getEntity();
        if(event.getFinalDamage() < player.getHealth()) return;
        event.setCancelled(true);
        Boolean keepInventory = player.getWorld().getGameRuleValue(GameRule.KEEP_INVENTORY);
        if(keepInventory != null && !keepInventory) {
            player.getInventory().forEach(item -> {if(item != null) player.getWorld().dropItemNaturally(player.getLocation(), item);});
            player.getInventory().clear();
            player.updateInventory();
        }
        killPLayer(player);
        if(event instanceof EntityDamageByEntityEvent) {
            broadcastDeathMessage(player, ((EntityDamageByEntityEvent) event).getDamager());
        } else {
            broadcastDeathMessage(player, null);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if(event.getTo() == null || event.getTo().getBlockY() > -60) return;
        Player player = event.getPlayer();
        Boolean keepInventory = player.getWorld().getGameRuleValue(GameRule.KEEP_INVENTORY);
        if(keepInventory != null && !keepInventory) {
            player.getInventory().clear();
            player.updateInventory();
        }
        Location spawn = player.getBedSpawnLocation() == null ? player.getWorld().getSpawnLocation() : player.getBedSpawnLocation();
        player.teleport(spawn);
        killPLayer(player);
        broadcastDeathMessage(player, null);
    }

    public void killPLayer(Player player) {
        if(player.getGameMode() == GameMode.SPECTATOR) return;
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
            boolean isInvulnerable = player.isInvulnerable();
            player.setInvulnerable(true);
            Bukkit.getScheduler().runTaskLater(ApvpPlugin.instance, () -> {
                if(!player.isOnline()) return;
                player.teleport(spawn);
                player.setGameMode(before);
                if(!isInvulnerable) {
                    Bukkit.getScheduler().runTaskLater(ApvpPlugin.instance, () -> {
                        if(!player.isOnline()) return;
                        player.setInvulnerable(false);
                    }, protectionTime * 20L);
                }
            }, timeout * 20L);
        }
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
        player.setFoodLevel(20);
        player.sendTitle(title, subtitle, 10, 20, 10);
        Bukkit.getOnlinePlayers().stream().filter(p -> !p.equals(player)).forEach(p -> p.playSound(player.getLocation(), soundOther, SoundCategory.PLAYERS,1.0f, 1.0f));
        player.playSound(player.getLocation(), soundSelf, SoundCategory.PLAYERS, 1.0f, 1.0f);
    }

    public void broadcastDeathMessage(Player player, Entity damager) {
        if(damager != null) {
            String message = pvpMessages.get(rng.nextInt(pvpMessages.size()));
            Bukkit.broadcastMessage(message.replace("%player%", player.getName()).replace("%killer%", damager.getName()));
        } else {
            String message = genericMessages.get(rng.nextInt(genericMessages.size()));
            Bukkit.broadcastMessage(message.replace("%player%", player.getName()));
        }
    }

    @Override
    public String getName() {
        return "Better Death";
    }
}
