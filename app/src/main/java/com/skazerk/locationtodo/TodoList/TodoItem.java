package com.skazerk.locationtodo.TodoList;

/**
 * Created by Skaze on 6/1/17.
 */

public class TodoItem {
    private String name;

    public TodoItem(String name) {
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
