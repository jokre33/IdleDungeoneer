package eu.jokre.games.idleDungeoneer;

import java.time.Duration;

/**
 * Created by jokre on 20-May-17.
 */

public class Settings {
    private Duration globalCooldown;

    public Settings() {
        this.globalCooldown = Duration.ofMillis(1500);
    }

    public Duration getGlobalCooldown() {
        return globalCooldown;
    }
}
