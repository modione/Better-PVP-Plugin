package me.sm.apvpplugin;

import java.util.logging.Level;
import me.sm.apvpplugin.base.AbstractModule;
import me.sm.apvpplugin.modules.DisableShieldsModule;
import me.sm.apvpplugin.modules.NoAttackCooldownModule;
import me.sm.apvpplugin.utils.FileConfig;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class ApvpPlugin extends JavaPlugin {
    public FileConfig config;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        System.out.println(getDataFolder().toPath().resolve("config.yml").toString());
        config = new FileConfig(getDataFolder().toPath().resolve("config.yml").toString());
        this.register();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        this.unregister();
    }

    public void register() {
        registerModule(new NoAttackCooldownModule());
        registerModule(new DisableShieldsModule());
    }

    public void unregister() {

    }

    public void registerModule(AbstractModule module) {
        try {
            if(!module.isEnabled(config)) return;
            Bukkit.getPluginManager().registerEvents(module, this);
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "An error occurred whilst trying to register modules!", e);
        }
    }
}
