package me.sm.apvpplugin.base;

import javax.naming.ConfigurationException;

import me.sm.apvpplugin.utils.FileConfig;
import org.bukkit.event.Listener;

public abstract class AbstractModule implements Listener {
    public abstract String getName(); // Disable Shields

    public boolean isEnabled(FileConfig config) throws ConfigurationException {
        String configName = getName().toLowerCase().replace(' ', '-') + "-enabled"; //disable-shields-enabled
        if(!config.contains(configName)) throw new ConfigurationException(
            String.format("This is an configuration issue AND NOT A BUG! The module '%s' could not be enabled because the option '%s' is not set! (Try deleting the config.yml file and reloading/restarting your server!)",
                getName(), configName));
        return config.getBoolean(configName);
    }
}
