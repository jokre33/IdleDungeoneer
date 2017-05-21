package eu.jokre.games.idleDungeoneer;

import java.time.Duration;

/**
 * Created by jokre on 20-May-17.
 */

public class Settings {
    private Duration globalCooldown;
    private double baseDodge;
    private double baseParry;
    private double baseCrit;

    public Settings() {
        this.globalCooldown = Duration.ofMillis(1500);
        this.baseDodge = 0.075;
        this.baseParry = 0.075;
        this.baseCrit = 0.05;
    }

    public Duration getGlobalCooldown() {
        return globalCooldown;
    }

    public double getBaseDodge() {
        return baseDodge;
    }

    public double getBaseParry() {
        return baseParry;
    }

    public double getBaseCrit() {
        return baseCrit;
    }
}
