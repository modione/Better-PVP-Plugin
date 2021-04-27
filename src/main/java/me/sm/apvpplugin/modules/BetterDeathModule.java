package me.sm.apvpplugin.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import me.sm.apvpplugin.ApvpPlugin;
import me.sm.apvpplugin.base.AbstractModule;
import me.sm.apvpplugin.utils.FileConfig;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.meta.FireworkMeta;

public class BetterDeathModule extends AbstractModule {
    private Sound soundOther;
    private Sound soundSelf;
    private String title;
    private String subtitle;
    private long timeout;
    private long protectionTime;
    private List<String> pvpMessages;
    private List<String> genericMessages;
    private DeathEffectType deathEffectType;
    private final List<UUID> spectators = new ArrayList<>();
    private final Random rng = new Random();

    public BetterDeathModule(FileConfig config) {
        try {
            soundOther = Sound.valueOf(config.getString("better-death.sound-other"));
            soundSelf = Sound.valueOf(config.getString("better-death.sound-self"));
            title = config.getString("better-death.title");
            subtitle = config.getString("better-death.subtitle");
            timeout = config.getLong("better-death.timeout");
            protectionTime = config.getLong("better-death.protection-time");
            System.out.println(config.getString("better-death.effect"));
            deathEffectType = DeathEffectType.valueOf(config.getString("better-death.effect"));
            pvpMessages = config.getStringList("custom-deathmsgs.pvp");
            genericMessages = config.getStringList("custom-deathmsgs.generic");
        } catch(Exception e) {
            ApvpPlugin.instance.getLogger().log(Level.WARNING, "This is an configuration issue AND NOT A BUG! The module 'BetterDeath' could not be enabled because the config is invalid! (Try deleting the config.yml file and reloading/restarting your server!)", e);
        }
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
            float exp = player.getExp();
            ExperienceOrb orb = ((ExperienceOrb) player.getWorld().spawnEntity(player.getLocation(), EntityType.EXPERIENCE_ORB));
            if(player.getLevel() > 14) {
                orb.setExperience(200);
            } else {
                orb.setExperience(Math.round(exp / 2));
            }
        }
        playDeathEffect(player, deathEffectType);
        killPLayer(player);
        if(event instanceof EntityDamageByEntityEvent) {
            Entity damager = ((EntityDamageByEntityEvent) event).getDamager();
            if (!(damager instanceof LivingEntity)) {
                try {
                    damager = (Entity) ((Projectile) damager).getShooter();
                }catch (ClassCastException e) {
                    broadcastDeathMessage(player, null);
                }
            }
            broadcastDeathMessage(player, damager);
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
            player.setExp(0.0f);
        }
        Location spawn = player.getBedSpawnLocation() == null ? player.getWorld().getSpawnLocation() : player.getBedSpawnLocation();
        player.teleport(spawn);
        killPLayer(player);
        broadcastDeathMessage(player, null);
    }

    public void killPLayer(Player player) {
        if(spectators.contains(player.getUniqueId())) return;
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
        player.setFoodLevel(20);
        player.sendTitle(title, subtitle, 10, 20, 10);
        Bukkit.getOnlinePlayers().stream().filter(p -> !p.equals(player)).forEach(p -> p.playSound(player.getLocation(), soundOther, SoundCategory.PLAYERS,1.0f, 1.0f));
        player.playSound(player.getLocation(), soundSelf, SoundCategory.PLAYERS, 1.0f, 1.0f);
        if(!Bukkit.isHardcore()) {
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
                spectators.add(player.getUniqueId());
                Bukkit.getScheduler().runTaskLater(ApvpPlugin.instance, () -> {
                    spectators.remove(player.getUniqueId());
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
        }
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

    public void playDeathEffect(Player player, DeathEffectType type) {
        switch(type) {
            case VANILLA:
                player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 10, 0.0, 0.0, 0.0);
                break;
            case LIGHTNING:
                player.getWorld().strikeLightningEffect(player.getLocation());
                break;
            case EXPLOSION:
                player.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, player.getLocation(), 1);
                Bukkit.getOnlinePlayers().forEach(p -> p.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS,1.0f, 1.0f));
                break;
            case RIP:
                AreaEffectCloud cloud = ((AreaEffectCloud) player.getWorld().spawnEntity(player.getLocation().add(0.0, 0.25, 0.0), EntityType.AREA_EFFECT_CLOUD));
                cloud.setDuration(5 * 20);
                cloud.setCustomName(ChatColor.RED + "R.I.P. " + ChatColor.GREEN + player.getName());
                cloud.setCustomNameVisible(true);
                cloud.setRadius(-0.0f);
                break;
            case FIREWORK:
                Firework fw = ((Firework) player.getWorld().spawnEntity(player.getLocation(), EntityType.FIREWORK));
                FireworkMeta meta = fw.getFireworkMeta();
                meta.addEffect(FireworkEffect.builder().trail(rng.nextBoolean()).flicker(rng.nextBoolean())
                    .with(Type.values()[rng.nextInt(Type.values().length)]).withColor(Color.fromBGR(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256)))
                    .withFade(Color.fromBGR(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256))).build());
                meta.setPower(2 + rng.nextInt(5));
                fw.setFireworkMeta(meta);
                break;
            case RANDOM:
                playDeathEffect(player, DeathEffectType.values()[rng.nextInt(DeathEffectType.values().length - 1)]);
                break;
        }
    }

    @Override
    public String getName() {
        return "Better Death";
    }

    private enum DeathEffectType {
        VANILLA, LIGHTNING, EXPLOSION, RIP, FIREWORK, RANDOM;
    }
}
