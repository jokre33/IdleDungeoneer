package eu.jokre.games.idleDungeoneer.Inventory;

import java.util.Vector;

/**
 * Created by jokre on 29-May-17.
 */
public class Inventory {
    Vector<Item> inventory = new Vector<>();

    public Inventory() {

    }

    public void addItem(Item item) {
        inventory.addElement(item);
    }

    public void removeItem(Item item) {
        inventory.remove(item);
    }

    public void draw() {

    }
}
