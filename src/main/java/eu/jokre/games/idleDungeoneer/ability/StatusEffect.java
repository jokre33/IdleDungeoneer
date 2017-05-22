package eu.jokre.games.idleDungeoneer.ability;

import java.time.Duration;
import java.time.Instant;

/**
 * Created by jokre on 20-May-17.
 */
public class StatusEffect {
    protected Duration duration;
    protected Instant endOfApplication;

    public StatusEffect() {

    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public Instant getEndOfApplication() {
        return endOfApplication;
    }

    public void setEndOfApplication(Instant endOfApplication) {
        this.endOfApplication = endOfApplication;
    }

    public void apply() {
        this.endOfApplication = Instant.now().plus(duration);
    }

    public void refresh() {
        this.endOfApplication = Instant.now().plus(duration);
    }
}
