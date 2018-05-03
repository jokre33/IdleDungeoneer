package eu.jokre.games.idleDungeoneer.Inventory;

import eu.jokre.games.idleDungeoneer.entity.PlayerCharacter;
import org.joml.Vector3f;

import java.util.Vector;
import java.util.concurrent.ThreadLocalRandom;

import static eu.jokre.games.idleDungeoneer.entity.PlayerCharacter.characterClasses.PRIEST;

/**
 * Created by jokre on 29-May-17.
 */
public class InventoryCharacter {
    PlayerCharacter owner;

    Item[] inventory = new Item[16];

    public InventoryCharacter(PlayerCharacter owner) {
        this.owner = owner;
    }

    public Item getItem(int slotID) {
        return inventory[slotID];
    }

    public Item getItem(Item.itemSlots slot) {
        return inventory[slot.getValue()];
    }

    public void generateSampleGear(int itemLevel, boolean tank) {
        Vector<Item.secondaryStat> possibleSecondaries = new Vector<>();
        if (tank) {
            possibleSecondaries.addElement(Item.secondaryStat.PARRY);
            possibleSecondaries.addElement(Item.secondaryStat.DODGE);
            possibleSecondaries.addElement(Item.secondaryStat.BLOCK);
        } else {
            possibleSecondaries.addElement(Item.secondaryStat.CRIT);
            possibleSecondaries.addElement(Item.secondaryStat.ACCURACY);
            possibleSecondaries.addElement(Item.secondaryStat.HASTE);
        }

        for (int i = 0; i < inventory.length; i++) {
            int sec1 = ThreadLocalRandom.current().nextInt(3);
            int sec2 = ThreadLocalRandom.current().nextInt(3);
            if (i < 14) {
                inventory[i] = new Item(itemLevel, Item.getItemType(Item.itemSlots.fromInt(i)).firstElement(), possibleSecondaries.elementAt(sec1), possibleSecondaries.elementAt(sec2), owner.getArmorClass());
            } else {
                switch (owner.getCharacterClass()) {
                    case PRIEST:
                        if (i == 14) {
                            inventory[i] = new Item(itemLevel, Item.itemTypes.WEAPON_2H, possibleSecondaries.elementAt(sec1), possibleSecondaries.elementAt(sec2), owner.getArmorClass());
                        }
                        break;
                    case MAGE:
                        if (i == 14) {
                            inventory[i] = new Item(itemLevel, Item.itemTypes.WEAPON_2H, possibleSecondaries.elementAt(sec1), possibleSecondaries.elementAt(sec2), owner.getArmorClass());
                        }
                        break;
                    case ROGUE:
                        inventory[i] = new Item(itemLevel, Item.itemTypes.WEAPON_1H, possibleSecondaries.elementAt(sec1), possibleSecondaries.elementAt(sec2), owner.getArmorClass());
                        break;
                    case WARRIOR:
                        if (i == 14) {
                            inventory[i] = new Item(itemLevel, Item.itemTypes.WEAPON_1H, possibleSecondaries.elementAt(sec1), possibleSecondaries.elementAt(sec2), owner.getArmorClass());
                        } else {
                            inventory[i] = new Item(itemLevel, Item.itemTypes.SHIELD, possibleSecondaries.elementAt(sec1), possibleSecondaries.elementAt(sec2), owner.getArmorClass());
                        }
                }
            }
        }
    }
}
