package com.skazerk.locationtodo.model;

/**
 * Created by Skaze on 6/1/17.
 */

public class TodoItem {
    private String itemName;
    private boolean history;

    public TodoItem() {
        this.itemName = "";
        this.history = false;
    }

    public String getItemName(){
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public void setHistory(boolean history) {
        this.history = history;
    }

    public boolean isHistory() {
        return history;
    }
}

