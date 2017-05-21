package eu.jokre.games.idleDungeoneer.Inventory;

import java.util.Vector;

import static eu.jokre.games.idleDungeoneer.Inventory.Item.itemSlots.*;

/**
 * Created by jokre on 21-May-17.
 */
public class Item {
    public enum itemSlots {
        HEAD(1),
        SHOULDERS(2),
        BACK(3),
        CHEST(4),
        WRISTS(5),
        HANDS(6),
        WAIST(7),
        LEGS(8),
        FEET(9),
        NECK(10),
        RING1(11),
        RING2(12),
        TRINKET1(13),
        TRINKET2(14),
        WEAPON1(15),
        WEAPON2(16);

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
        HELMET(1),
        PAULDRONS(2),
        CLOAK(3),
        BODY_ARMOR(4),
        BRACERS(5),
        GLOVES(6),
        BELT(7),
        PANTS(8),
        SHOES(9),
        AMULET(10),
        RING(11),
        TRINKET(12),
        WEAPON_1H(13),
        WEAPON_2H(14),
        SHIELD(15);

        private int _value;

        itemTypes(int Value) {
            this._value = Value;
        }

        public int getValue() {
            return _value;
        }

        public static itemTypes fromInt(int i) {
            for (itemTypes a : itemTypes.values()) {
                if (a.getValue() == i) return a;
            }
            return null;
        }
    }

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

    private double[] itemTypeStatFactor = new double[15];

    private int itemLevel;
    private itemTypes itemType;

    public Item(int itemLevel, itemTypes type) {
        this.itemLevel = itemLevel;
        this.itemType = type;
    }

    public Vector<itemSlots> getItemSlot() {
        return this.getItemSlot(this.getItemType());
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
        switch (itemSlot) {
            case HEAD:
                return 1;
            case SHOULDERS:
                return 2;
            case BACK:
                return 3;
            case CHEST:
                return 4;
            case WRISTS:
                return 5;
            case HANDS:
                return 6;
            case WAIST:
                return 7;
            case LEGS:
                return 8;
            case FEET:
                return 9;
            case NECK:
                return 10;
            case RING1:
                return 11;
            case RING2:
                return 12;
            case TRINKET1:
                return 13;
            case TRINKET2:
                return 14;
            case WEAPON1:
                return 15;
            case WEAPON2:
                return 16;
            default:
                return 0;
        }
    }

    public itemTypes getItemType() {
        return this.itemType;
    }
}
