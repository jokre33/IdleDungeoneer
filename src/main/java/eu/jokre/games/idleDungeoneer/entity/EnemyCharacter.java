package eu.jokre.games.idleDungeoneer.entity;

import eu.jokre.games.idleDungeoneer.IdleDungeoneer;
import eu.jokre.games.idleDungeoneer.ability.Ability;
import eu.jokre.games.idleDungeoneer.ability.AutoAttack;
import org.joml.Vector2f;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Vector;

/**
 * Created by jokre on 20-May-17.
 */
public class EnemyCharacter extends EntityCharacter {
    public enum Classification {
        NORMAL,
        ELITE,
        RARE_ELITE,
        DUNGEON_ELITE,
        DUNGEON_BOSS,
        RAID_ELITE,
        RAID_BOSS
    }

    public EnemyCharacter(int level, float itemlevel, Vector2f position, String name, Classification classification) {
        super(level, itemlevel, position, name, true);
        IdleDungeoneer.idleDungeoneer.generateAggroOnPlayerCharacters(this, 1);
        this.addAbility(new AutoAttack(), 0);

        double damageMod = 0;
        double healthMod = 0;

        switch (classification) {
            case NORMAL:
                damageMod = 1;
                healthMod = 1;
                break;
            case ELITE:
                damageMod = 1.2;
                healthMod = 2;
                break;
            case RARE_ELITE:
                damageMod = 1.4;
                healthMod = 3;
                break;
            case DUNGEON_ELITE:
                damageMod = 2;
                healthMod = 5;
                break;
            case DUNGEON_BOSS:
                damageMod = 3;
                healthMod = 20;
                break;
            case RAID_ELITE:
                damageMod = 3;
                healthMod = 20;
                break;
            case RAID_BOSS:
                damageMod = 5;
                healthMod = 50;
                break;
            default:
                damageMod = 99999;
                healthMod = 99999;
                break;
        }
        this.weaponDamageMin *= damageMod;
        this.weaponDamageMax *= damageMod;
        this.health *= healthMod;
        this.maximumHealth *= healthMod;
    }

    public void ai() {
        if (this.hasTarget() && !this.getTarget().isDead()) {
            Ability nextCast = this.chooseNextCast();
            if (nextCast != null) {
                attack(this.target, nextCast);
            }
        } else {
            findTarget();
        }
    }

    public boolean tick() {
        Instant now = Instant.now();
        long timeSinceLastTick = ChronoUnit.MILLIS.between(this.lastTick, now);
        this.lastTick = now;

        if (this.resource < this.maximumResource) {
            this.resource += timeSinceLastTick * resourceRegeneration / 1000;
        }
        if (this.resource > this.maximumResource) {
            this.resource = this.maximumResource;
        }
        if (this.isDead()) {
            return false;
        }
        this.ai();
        return true;
    }
}
