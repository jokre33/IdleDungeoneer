package eu.jokre.games.idleDungeoneer.entity;

import eu.jokre.games.idleDungeoneer.IdleDungeoneer;
import eu.jokre.games.idleDungeoneer.Inventory.Item;
import eu.jokre.games.idleDungeoneer.ability.AbilityWarriorSlam;
import eu.jokre.games.idleDungeoneer.ability.AbilityWarriorSlam2;
import eu.jokre.games.idleDungeoneer.ability.AbilityWarriorTaunt;
import eu.jokre.games.idleDungeoneer.ability.AutoAttackWarrior;
import org.joml.Vector2d;

import java.time.Duration;
import java.time.Instant;
import java.util.Vector;

import static eu.jokre.games.idleDungeoneer.IdleDungeoneer.idleDungeoneer;

/**
 * Created by jokre on 22-May-17.
 */
public class PlayerCharacterWarrior extends PlayerCharacter {

    private Vector<EnemyCharacter> tauntList = new Vector<>();
    private Instant tauntlistUpdateCooldown = Instant.now();

    public PlayerCharacterWarrior(int level, Vector2d position, String name) {
        super(level, position, name);
        this.armorClass = Item.armorClass.PLATE;
        this.characterClass = characterClasses.WARRIOR;
        this.abilities[0] = new AutoAttackWarrior(this);
        this.abilities[1] = new AutoAttackWarrior(this);
        this.abilities[0].enable();
        this.abilities[1].disable();
        this.setAggroModifier(10);
        this.attackPower = 5500;
        this.weaponDamageMin = attackPower * 0.95;
        this.weaponDamageMax = attackPower * 1.05;
        this.weaponAttackSpeed = Duration.ofMillis(2000);
        this.weapon2DamageMin = attackPower * 0.95;
        this.weapon2DamageMax = attackPower * 1.05;
        this.weapon2AttackSpeed = Duration.ofMillis(1800);
        this.abilities[0].setCooldown(weaponAttackSpeed);
        this.abilities[1].setCooldown(weapon2AttackSpeed);
        this.blockChance = 0.4;
        this.dodgeChance = 0.2;
        this.parryChance = 0.2;
        this.blockAmount = 10000;
        this.armorDamageReduction = 0.54;
        this.maximumHealth = this.stamina * 5 * 2;
        this.health = this.maximumHealth;
        this.setTank(true);
        this.resourceType = resourceTypes.RAGE;
        this.maximumResource = 100;
        this.resource = 0;
        this.resourceRegeneration = 1;
        this.addAbility(new AbilityWarriorSlam(this), 20);
        this.addAbility(new AbilityWarriorSlam2(this), 19);
        this.addAbility(new AbilityWarriorTaunt(this), 18);
        this.inventory.generateSampleGear(145, true);
        this.updateStats();
    }

    @Override
    protected void abilityPriorityList() {
        if (abilities[18].cooldownReady()) {
            if (tauntlistUpdateCooldown.isBefore(Instant.now())) {
                updateTauntList();
            }
            if (!tauntList.isEmpty()) {
                useAbility(tauntList.firstElement(), abilities[18]);
            } else {
                tauntlistUpdateCooldown = Instant.now().plusSeconds(2);
            }
        }
        if (useAbility(this.target, abilities[19])) return;
        if (useAbility(this.target, abilities[20])) return;
    }

    public void takeDamage(EntityCharacter t, double a) {
        super.takeDamage(t, a);
        this.resource += a / this.maximumHealth * 100;
        if (this.resource > this.maximumResource) this.resource = this.maximumResource;
    }

    private void updateTauntList() {
        for (EnemyCharacter enemyCharacter : idleDungeoneer.getEnemyCharacters()) {
            if (!enemyCharacter.getTarget().isTank() && !tauntList.contains(enemyCharacter)) {
                tauntList.add(enemyCharacter);
            } else if (enemyCharacter.getTarget().isTank() && tauntList.contains(enemyCharacter)) {
                tauntList.remove(enemyCharacter);
            }
        }
    }
}
