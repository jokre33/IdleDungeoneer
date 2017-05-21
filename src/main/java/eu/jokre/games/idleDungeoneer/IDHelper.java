package eu.jokre.games.idleDungeoneer;

/**
 * Created by jokre on 20-May-17.
 */
public class IDHelper {
    private double levelDodge;
    private double levelParry;
    private double critSuppression;
    private double baseCrush;
    private double levelCrush;
    private double baseMiss;

    public IDHelper() {
        this.levelDodge = 0.015;
        this.levelParry = 0.015;
        this.critSuppression = 0.01;
        this.baseCrush = -0.09;
        this.levelCrush = 0.03;
        this.baseMiss = 0.05;
    }

    public int physicalHitRoll(double accuracy, double dodge, double parry, double block, double crit, int attackerLevel, int defenderLevel) { //0 = Miss, 1 = Dodge, 2 = Parry, 3 = Block, 4 = Crit, 5 = Crush, 6 = Hit
        double miss = baseMiss;
        double crush = baseCrush;

        if (attackerLevel > defenderLevel) {
            dodge -= levelDodge * (attackerLevel - defenderLevel);
            if (dodge < 0) dodge = 0;
            parry -= levelParry * (attackerLevel - defenderLevel);
            if (parry < 0) parry = 0;
            crush += levelCrush * (attackerLevel - defenderLevel);
        } else if (attackerLevel < defenderLevel) {
            dodge += levelDodge * (defenderLevel - attackerLevel);
            parry += levelParry * (defenderLevel - attackerLevel);
            crit -= critSuppression * (defenderLevel - attackerLevel);
            if (crit < 0) crit = 0;
        }

        if (crush < 0) crush = 0;

        if (accuracy < baseMiss) {
            miss -= accuracy;
        } else if (accuracy >= baseMiss && accuracy < (baseMiss + dodge)) {
            miss = 0;
            dodge -= accuracy - miss;
        } else if (accuracy >= (baseMiss + dodge) && accuracy < (baseMiss + dodge + parry)) {
            miss = 0;
            dodge = 0;
            parry -= accuracy - miss - dodge;
        } else if (accuracy >= (baseMiss + dodge + parry)) {
            miss = 0;
            dodge = 0;
            parry = 0;
            crit += accuracy - baseMiss - dodge - parry;
        }
        double roll = Math.random();
        if (roll < miss) return 0;
        if (roll < (miss + dodge)) return 1;
        if (roll < (miss + dodge + parry)) return 2;
        if (roll < (miss + dodge + parry + block)) return 3;
        if (roll < (miss + dodge + parry + block + crit)) return 4;
        if (roll < (miss + dodge + parry + block + crit + crush)) return 5;
        return 6;
    }

    public int magicalHitRoll(double accuracy, double crit, int attackerLevel, int defenderLevel) {
        crit += accuracy;
        if (attackerLevel < defenderLevel) {
            crit -= critSuppression * (defenderLevel - attackerLevel);
            if (crit < 0) crit = 0;
        }
        double roll = Math.random();
        if (roll < crit) return 4;
        return 6;
    }
}
