package eu.jokre.games.idleDungeoneer.Inventory;

import eu.jokre.games.idleDungeoneer.entity.EntityCharacter;

import java.util.Vector;

import static eu.jokre.games.idleDungeoneer.Inventory.Item.itemSlots.*;
import static eu.jokre.games.idleDungeoneer.Inventory.Item.itemTypes.*;
import static eu.jokre.games.idleDungeoneer.Inventory.Item.primaryStat.*;

/**
 * Created by jokre on 21-May-17.
 */
public class Item {
    public enum itemSlots {
        HEAD(0),
        SHOULDERS(1),
        BACK(2),
        CHEST(3),
        WRISTS(4),
        HANDS(5),
        WAIST(6),
        LEGS(7),
        FEET(8),
        NECK(9),
        RING1(10),
        RING2(11),
        TRINKET1(12),
        TRINKET2(13),
        WEAPON1(14),
        WEAPON2(15);

        private int _value;

        itemSlots(int Value) {
            this._value = Value;
        }

        public int getValue() {
            return _value;
        }

        public static itemSlots fromInt(int i) {
            for (itemSlots a : itemSlots.values()) {
                if (a.getValue() == i) return a;
            }
            return null;
        }
    }

    public enum itemTypes {
        HELMET(0, 1.0f, 1.0f),
        PAULDRONS(1, 0.8f, 0.8f),
        CLOAK(2, 0.6f, 0.6f),
        BODY_ARMOR(3, 1.0f, 1.0f),
        BRACERS(4, 0.6f, 0.6f),
        GLOVES(5, 0.8f, 0.8f),
        BELT(6, 0.6f, 0.6f),
        PANTS(7, 1.0f, 1.0f),
        SHOES(8, 0.8f, 0.8f),
        AMULET(9, 1.0f, 0.0f),
        RING(10, 0.6f, 0.0f),
        TRINKET(11, 0f, 0.0f), //----
        WEAPON_1H(12, 1.0f, 0.0f),
        WEAPON_2H(13, 2.0f, 0.0f),
        SHIELD(14, 1.0f, 2.0f);

        private int _value;
        private float _statMult;
        private float _armorMult;

        itemTypes(int Value, float statMult, float armorMult) {
            this._value = Value;
            this._statMult = statMult;
            this._armorMult = armorMult;
        }

        public int getValue() {
            return _value;
        }

        public float getArmorMultiplier() {
            return _armorMult;
        }

        public float getStatMultiplier() {
            return _statMult;
        }

        public static itemTypes fromInt(int i) {
            for (itemTypes a : itemTypes.values()) {
                if (a.getValue() == i) return a;
            }
            return null;
        }
    }

    public static final float itemArmorFactorCloth = 0.2f;      //~790  (13.55%) Armor with full Ilvl 70 Gear
    public static final float itemArmorFactorLeather = 0.4f;    //~1600 (23.87%) Armor
    public static final float itemArmorFactorMail = 0.6f;       //~2350 (31.99%) Armor
    public static final float itemArmorFactorPlate = 1.0f;      //~3900 (43.95%) Armor or ~5000 (50.00%) with a Shield

    //Stats of a Itemlevel 70 Item at a Scalefactor of 1.0
    public static final int itemLevelStatBase = 70;
    public static final int baseFullSetPrimary = 4167;
    public static final int baseFullSetStamina = 5208;
    public static final double baseFullSetWeaponDPS = baseFullSetPrimary * 0.8;
    public static final int baseItemArmor = 543;
    public static final int baseItemPrimary = 365;
    public static final int baseItemStamina = 457;
    public static final int baseItemSecondary1 = 256;
    public static final int baseItemSecondary2 = 183;

    public enum primaryStat {
        STRENGTH,
        AGILITY,
        INTELLIGENCE
    }

    public enum secondaryStat {
        CRIT,
        HASTE,
        ACCURACY,
        PARRY,
        DODGE,
        BLOCK
    }

    public enum armorClass {
        CLOTH(INTELLIGENCE, 0.2f),
        LEATHER(AGILITY, 0.4f),
        MAIL(AGILITY, 0.6f),
        PLATE(STRENGTH, 1.0f);

        private primaryStat _primary;
        private float _mult;

        armorClass(primaryStat p, float mult) {
            this._primary = p;
            this._mult = mult;
        }

        primaryStat getPrimaryStat() {
            return this._primary;
        }

        float getArmorMultiplier() {
            return this._mult;
        }
    }

    private int itemLevel;
    private itemTypes itemType;
    private primaryStat primaryStat;
    private secondaryStat secondaryStat1;
    private secondaryStat secondaryStat2;
    private armorClass armorClass;

    private long primaryStatAmount;
    private long staminaAmount;
    private long secondaryStat1Amount;
    private long secondaryStat2Amount;
    private long armorAmount;

    private long minDamage;
    private long maxDamage;
    private float swingTime;

    public Item(int itemLevel, itemTypes type, secondaryStat secondaryStat1, secondaryStat secondaryStat2, armorClass armorClass) {
        this.itemLevel = itemLevel;
        this.itemType = type;
        this.primaryStat = armorClass.getPrimaryStat();
        this.secondaryStat1 = secondaryStat1;
        this.secondaryStat2 = secondaryStat2;
        this.armorClass = armorClass;

        double itemLevelStatMultiplier = Math.pow(EntityCharacter.itemLevelScaling, (this.itemLevel - itemLevelStatBase) / EntityCharacter.itemLevelScalingPerXAmount);
        double itemArmorMultiplier = itemLevelStatMultiplier * this.itemType.getArmorMultiplier() * this.armorClass.getArmorMultiplier();
        double itemStatMultiplier = itemLevelStatMultiplier * this.itemType.getStatMultiplier();

        this.primaryStatAmount = Math.round(baseItemPrimary * itemStatMultiplier);
        this.secondaryStat1Amount = Math.round(baseItemSecondary1 * itemStatMultiplier);
        this.secondaryStat2Amount = Math.round(baseItemSecondary2 * itemStatMultiplier);
        this.staminaAmount = Math.round(baseItemStamina * itemStatMultiplier);
        this.armorAmount = Math.round(baseItemArmor * itemArmorMultiplier);

        switch (type) {
            case WEAPON_1H:
                this.swingTime = 2.0f;
                this.minDamage = Math.round(this.swingTime * baseFullSetWeaponDPS * 0.9);
                this.maxDamage = Math.round(this.swingTime * baseFullSetWeaponDPS * 1.1);
                break;
            case WEAPON_2H:
                this.swingTime = 3.6f;
                this.minDamage = Math.round(this.swingTime * baseFullSetWeaponDPS * 0.9);
                this.maxDamage = Math.round(this.swingTime * baseFullSetWeaponDPS * 1.1);
                break;
            default:
                break;
        }
    }

    public Vector<itemSlots> getItemSlot() {
        return this.getItemSlot(this.getItemType());
    }

    public static Vector<itemTypes> getItemType(itemSlots slot) {
        Vector<itemTypes> itemTypes = new Vector<>();
        switch (slot) {
            case HEAD:
                itemTypes.addElement(HELMET);
                break;
            case SHOULDERS:
                itemTypes.addElement(PAULDRONS);
                break;
            case BACK:
                itemTypes.addElement(CLOAK);
                break;
            case CHEST:
                itemTypes.addElement(BODY_ARMOR);
                break;
            case WRISTS:
                itemTypes.addElement(BRACERS);
                break;
            case HANDS:
                itemTypes.addElement(GLOVES);
                break;
            case WAIST:
                itemTypes.addElement(BELT);
                break;
            case LEGS:
                itemTypes.addElement(PANTS);
                break;
            case FEET:
                itemTypes.addElement(SHOES);
                break;
            case NECK:
                itemTypes.addElement(AMULET);
                break;
            case RING1:
                itemTypes.addElement(RING);
                break;
            case RING2:
                itemTypes.addElement(RING);
                break;
            case TRINKET1:
                itemTypes.addElement(TRINKET);
                break;
            case TRINKET2:
                itemTypes.addElement(TRINKET);
                break;
            case WEAPON1:
                itemTypes.addElement(WEAPON_1H);
                itemTypes.addElement(WEAPON_2H);
            case WEAPON2:
                itemTypes.addElement(WEAPON_1H);
                itemTypes.addElement(SHIELD);
        }
        return itemTypes;
    }

    private Vector<itemSlots> getItemSlot(itemTypes itemType) {
        Vector<itemSlots> itemSlots = new Vector<>();
        switch (itemType) {
            case HELMET:
                itemSlots.addElement(HEAD);
                break;
            case PAULDRONS:
                itemSlots.addElement(SHOULDERS);
                break;
            case CLOAK:
                itemSlots.addElement(BACK);
                break;
            case BODY_ARMOR:
                itemSlots.addElement(CHEST);
                break;
            case BRACERS:
                itemSlots.addElement(WRISTS);
                break;
            case GLOVES:
                itemSlots.addElement(HANDS);
                break;
            case BELT:
                itemSlots.addElement(WAIST);
                break;
            case PANTS:
                itemSlots.addElement(LEGS);
                break;
            case SHOES:
                itemSlots.addElement(FEET);
                break;
            case AMULET:
                itemSlots.addElement(NECK);
                break;
            case RING:
                itemSlots.addElement(RING1);
                itemSlots.addElement(RING2);
                break;
            case TRINKET:
                itemSlots.addElement(TRINKET1);
                itemSlots.addElement(TRINKET2);
                break;
            case WEAPON_2H:
                itemSlots.addElement(WEAPON1);
                break;
            case WEAPON_1H:
                itemSlots.addElement(WEAPON1);
                itemSlots.addElement(WEAPON2);
                break;
            case SHIELD:
                itemSlots.addElement(WEAPON2);
                break;
        }
        return itemSlots;
    }

    public static itemSlots getItemSlot(int itemSlotID) {
        return itemSlots.fromInt(itemSlotID);
    }

    public static int getItemSlotID(itemSlots itemSlot) {
        return itemSlot.getValue();
    }

    public itemTypes getItemType() {
        return this.itemType;
    }

    public int getItemLevel() {
        return itemLevel;
    }

    public Item.primaryStat getPrimaryStat() {
        return primaryStat;
    }

    public secondaryStat getSecondaryStat1() {
        return secondaryStat1;
    }

    public secondaryStat getSecondaryStat2() {
        return secondaryStat2;
    }

    public long getPrimaryStatAmount() {
        return primaryStatAmount;
    }

    public long getStaminaAmount() {
        return staminaAmount;
    }

    public long getSecondaryStat1Amount() {
        return secondaryStat1Amount;
    }

    public long getSecondaryStat2Amount() {
        return secondaryStat2Amount;
    }

    public Item.armorClass getArmorClass() {
        return armorClass;
    }

    public long getArmorAmount() {
        return armorAmount;
    }

    public long getMinDamage() {
        return minDamage;
    }

    public long getMaxDamage() {
        return maxDamage;
    }

    public float getSwingTime() {
        return swingTime;
    }
}
