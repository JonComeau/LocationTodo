package com.skazerk.locationtodo.model;

import com.skazerk.locationtodo.model.TodoItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Skaze on 6/1/17.
 */

public class TodoList {
    private String listName;
    private Location location;
    public ArrayList<TodoItem> listItems = new ArrayList<>();

    public class Location {
        private float[] latLong;
        private String address;

        public Location() {
            this.latLong = new float[2];
            this.latLong[0] = 0;
            this.latLong[1] = 0;
            this.address = "";
        }

        public Location(float latitude, float longitude, String address) {
            this.latLong = new float[2];
            this.latLong[0] = latitude;
            this.latLong[1] = longitude;
            this.address = address;
        }

        public float getLong() {
            return latLong[1];
        }

        public float getLat() {
            return latLong[0];
        }

        public void setLong(long longitude) {
            this.latLong[1] = longitude;
        }

        public void setLat(long latitude) {
            this.latLong[0] = latitude;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }
    }

    public TodoList() {
        this.listName = "";
        this.location = new Location();
    }

    public String getListName() {
        return listName;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(float latitude, float longitude, String address) {
        this.location = new Location(latitude, longitude, address);
    }

    public ArrayList<TodoItem> getListItems() {
        return listItems;
    }

    public void setListItems(ArrayList<TodoItem> listItems) {
        this.listItems = listItems;
    }
}
