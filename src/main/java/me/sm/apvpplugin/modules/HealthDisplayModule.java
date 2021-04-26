package me.sm.apvpplugin.modules;

import me.sm.apvpplugin.ApvpPlugin;
import me.sm.apvpplugin.base.AbstractModule;
import me.sm.apvpplugin.utils.FileConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.Objects;

public class HealthDisplayModule extends AbstractModule {
    public ScoreboardManager manager = Bukkit.getScoreboardManager();
    public Scoreboard scoreboard = Objects.requireNonNull(manager).getMainScoreboard();
    private boolean showabove;
    private boolean showtab;
    Objective objective;
    String name;
    String colortab;
    public HealthDisplayModule(FileConfig config) {
        showabove = config.getBoolean("show-hp-above.show-above");
        showtab = config.getBoolean("show-hp-above.show-on-tab");
        if (showabove) {
            name = config.getString("show-hp-above.name-of-team");
            assert name != null;
            if (scoreboard.getObjective(name) != null) {
                objective = scoreboard.getObjective(name);
                assert objective != null;
            }else {
                objective = scoreboard.registerNewObjective(name, Criterias.HEALTH, name);
            }
            objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
            Bukkit.getScheduler().scheduleSyncRepeatingTask(ApvpPlugin.instance, () -> {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    Score score = objective.getScore(player);
                    score.setScore((int) player.getHealth());
                }
            }, 10, 1);
        }
        if (showtab) {
            colortab = config.getString("show-hp-above.hp-color");
            Bukkit.getScheduler().scheduleSyncRepeatingTask(ApvpPlugin.instance, ()-> {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    String namea = player.getDisplayName();
                    String nameb = namea+" "+colortab+((int)player.getHealth());
                    player.setPlayerListName(nameb);
                }
            }, 10, 1);
        }
    }
    @Override
    public String getName() {
        return "Show Health above Player";
    }
}
