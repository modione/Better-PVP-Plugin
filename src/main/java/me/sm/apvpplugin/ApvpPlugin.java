package me.sm.apvpplugin;

import org.bukkit.plugin.java.JavaPlugin;

public final class ApvpPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.register();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        this.unregister();
    }
    public void register() {

    }
    public void unregister() {

    }
}
