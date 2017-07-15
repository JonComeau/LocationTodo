package com.skazerk.locationtodo.model;

import java.util.ArrayList;
import java.util.List;

public class DerpData{
    private static final String[] items = {"Item 1", "Item 2", "Item 3", "Item 4", "Item 5",
            "Item 6", "Item 7", "Item 8", "Item 9", "Item 10", "Item 11", "Item 12", "Item 13",
            "Item 14", "Item 15", "Item 16, this is item 16 and for some reason its alone"};

    private static String[] lists = {"List 1", "List 2", "List 3", "List 4",
            "List 5", "List 6", "List 7", "List 8", "List 9","List 10", "List 11","List 12"};

    public static ArrayList<TodoList> getListData(){
        ArrayList<TodoList> data = new ArrayList<>();

        for(int i = 0; i < lists.length; i++){
            TodoList list = new TodoList();
            list.setListName(lists[i]);
            for(int j = 0; j < items.length; j++){
                TodoItem item = new TodoItem();
                item.setItemName(items[j]);
                list.listItems.add(item);
            }
            data.add(list);
        }
        return data;
    }



}
