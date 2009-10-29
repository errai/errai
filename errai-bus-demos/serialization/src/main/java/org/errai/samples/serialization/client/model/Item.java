package org.errai.samples.serialization.client.model;

import org.jboss.errai.bus.server.annotations.ExposeEntity;

import java.io.Serializable;

@ExposeEntity
public class Item implements Serializable {
    private String itemName;
    private int quantity;

    public Item() {
    }

    public Item(int quantity, String itemName) {
        this.quantity = quantity;
        this.itemName = itemName;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "[" + quantity + " x " + itemName + "]";
    }
}
