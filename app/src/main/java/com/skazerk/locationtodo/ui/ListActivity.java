package com.skazerk.locationtodo.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.skazerk.locationtodo.Location.Location;
import com.skazerk.locationtodo.R;
import com.skazerk.locationtodo.adapter.MyAdapter;
import com.skazerk.locationtodo.model.TodoItem;
import com.skazerk.locationtodo.model.TodoList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListActivity extends AppCompatActivity{
    private static final String TAG = "LIST_ACTIVITY";
    private final Context context  = this;

    private File listFile;
    private TodoList theList;
    String listName;

    private RecyclerView listView;
    private TextView title;
    private TextView address;
    private MyAdapter itemAdapter;

    JSONObject listObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        Intent intent = getIntent();
        listName = intent.getStringExtra(Main.EXTRA_LISTFILE);
        listFile = new File(context.getFilesDir(), listName.toLowerCase() + ".json");

        try {
            if (listFile.createNewFile()) {
                listObject = new JSONObject();

                FileOutputStream outputStream = new FileOutputStream(listFile);
                outputStream.write(
                        (
                                "{" +
                                        "'title':'" + listName + "'" + "," +
                                        "'location':{" +
                                        "'long':'0'," +
                                        "'lat':'0'," +
                                        "'address':''}," +
                                        "'items':[]" +
                                        "}"
                        ).getBytes()
                );
                outputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        theList = grabJSONListData();

        listView = (RecyclerView) findViewById(R.id.listitems);
        listView.setLayoutManager(new LinearLayoutManager(this));
        itemAdapter = new MyAdapter(theList, this);
        listView.setAdapter(itemAdapter);

        title = (TextView) findViewById(R.id.title);
        title.setText(listName);

        address = (TextView) findViewById(R.id.address);
        if (theList.getLocation().getAddress().equals("")) {
            address.setText("Touch to set a location");
        } else {
            address.setText(theList.getLocation().getAddress());
        }
        address.setClickable(true);
        address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), Location.class);
                intent.putExtra(Main.EXTRA_LISTFILE, getIntent().getStringExtra(Main.EXTRA_LISTFILE));
                startActivityForResult(intent,1);
            }
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(createHelperCallback());
        itemTouchHelper.attachToRecyclerView(listView);

        FloatingActionButton addItem = (FloatingActionButton) findViewById(R.id.add_item);
        addItem.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                addItem();
            }
        });
    }

    /**
     * This function returns a callback for a list item so that different events are
     * handled. Only moving an item and swiping an item are handled
     *
     * @return ItemTouchHelper.Callback
     */
    private ItemTouchHelper.Callback createHelperCallback(){
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback =
                new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(RecyclerView recView, RecyclerView.ViewHolder viewHolder,
                                          RecyclerView.ViewHolder target){
                        moveItem(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                        return true;
                    }

                    @Override
                    public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir){
                        deleteItem(viewHolder.getAdapterPosition());
                    }
                };
        return simpleItemTouchCallback;
    }

    /**
     * This function is called when the startActivityforResult function is called.
     * The parameters contain the data that the activity needs. requestCode contains
     * the type of request that the requested activity sent.<br/>
     * The result code is sent by the activity and can be either ok or cancelled.<br/>
     * The Intent data is the intent that was sent back to this activity.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            JSONObject locationJSONObj = new JSONObject();
            try {
                saveLocation(data.getStringExtra("JSON"));
                locationJSONObj = new JSONObject(data.getStringExtra("JSON"));
                address.setText(locationJSONObj.get("address").toString());
                Log.d(TAG, "set address");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Takes the json string parameter and takes out the lat, long, and address string values
     * The values are saved into the list's json file in the location value. The list's json
     * is saved back into the internal storage with the changed location
     *
     * @param json
     */
    private void saveLocation(String json) {
        try {
            JSONObject locationJSONObj = new JSONObject(json);

            FileInputStream fileInputStream = new FileInputStream(listFile);
            byte[] bytes = new byte[(int) listFile.length()];

            fileInputStream.read(bytes);
            String itemJSONStr = new String(bytes);
            fileInputStream.close();

            JSONObject itemJSONObj = new JSONObject(itemJSONStr);
            JSONObject itemJSONLocationStr = itemJSONObj.getJSONObject("location");

            itemJSONLocationStr.put("lat", locationJSONObj.get("lat").toString());
            itemJSONLocationStr.put("long", locationJSONObj.get("long").toString());
            itemJSONLocationStr.put("address", locationJSONObj.get("address").toString());

            FileOutputStream fileOutputStream = new FileOutputStream(listFile);
            fileOutputStream.write(itemJSONObj.toString().getBytes());
            fileOutputStream.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Reads in the list's json file. then it populates the TodoList object which
     * holds the title, location, and items of the list.
     *
     * @return a new TodoList with the values filled
     */
    private TodoList grabJSONListData() {
        Log.d(TAG, "Line " + Thread.currentThread().getStackTrace()[2].getLineNumber
                () + " " + "grabbing data");
        TodoList tempList = new TodoList();

        FileInputStream inputStream = null;
        byte[] bytes = new byte[(int) listFile.length()];
        String listJSONStr;
        try {
            inputStream = new FileInputStream(listFile);
            inputStream.read(bytes);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        listJSONStr = new String(bytes);

        Log.d(TAG, "Line " + Thread.currentThread().getStackTrace()[2].getLineNumber
                () + " " + "opened " + listName + ".json");
        Log.d(TAG, "Line " + Thread.currentThread().getStackTrace()[2].getLineNumber
                () + " " + "grabbed JSONStr");
        Log.d(TAG, "Line " + Thread.currentThread().getStackTrace()[2].getLineNumber
                () + " " + listJSONStr);

        try {
            JSONObject listJSON = new JSONObject(listJSONStr); // create object from the string
            JSONObject listLocation = listJSON.getJSONObject("location");
            JSONArray listJSONArray = listJSON.getJSONArray("items"); // make an arraylist of items from the obj
            Log.d(TAG, "Line " + Thread.currentThread().getStackTrace()[2].getLineNumber
                    () + " " + "got listArray");
            Log.d(TAG, "Line " + Thread.currentThread().getStackTrace()[2].getLineNumber
                    () + " " + "masterJSONArray.length(): " + listJSONArray.length());

            tempList = new TodoList(); // make the list

            tempList.setListName(listJSON.getString("title")); // populate the list
            tempList.setLocation(
                    ((listLocation.getString("long").equals("")) ? 0 : Float.parseFloat
                            (listLocation.getString("long"))),
                    ((listLocation.getString("lat").equals("")) ? 0 :Float.parseFloat
                            (listLocation.getString("lat"))),
                    listLocation.getString("address")
            );

            Log.d(TAG, "Location: " + tempList.getLocation().getAddress());

            ArrayList<TodoItem> itemsList = new ArrayList<>();
            for(int i = 0; i < listJSONArray.length(); i++){
                TodoItem item = new TodoItem();
                JSONObject itemObject = listJSONArray.getJSONObject(i);

                item.setItemName(itemObject.getString("name"));
                Log.d(TAG, "Line " + Thread.currentThread().getStackTrace()[2].getLineNumber
                        () + " " + "added " + itemObject.getString("name"));

                //item.setHistory(itemObject.getBoolean("checked"));
                itemsList.add(item);
            }
            tempList.setListItems(itemsList);

        } catch (JSONException je) {
            je.printStackTrace();
        }

        return tempList;
    }

    /**
     * Reads in the json from an internal file, then takes the item json array.
     * the item object is then read into an arraylist and the new item is added to the end
     * of the array. This array is sorted by the Collections class and the sorted array
     * is written back into the json array and then back into the file.
     *
     * @param name The name of the item added into the list.
     */
    private void insertIntoList(String name) {
        ArrayList<String> temp = new ArrayList<>();

        FileInputStream inputStream = null;
        byte[] bytes = new byte[(int) listFile.length()];
        String listJSONStr;

        try {
            inputStream = new FileInputStream(listFile);
            inputStream.read(bytes);
            inputStream.close();
            listJSONStr = new String(bytes);

            JSONObject masterJSONList = new JSONObject(listJSONStr);
            JSONArray masterJSONItems = masterJSONList.getJSONArray("items");

            for (int i = 0; i < masterJSONItems.length(); i++) {
                JSONObject JSONItem = masterJSONItems.getJSONObject(i);
                temp.add(JSONItem.getString("name"));
            }

            temp.add(name);

            Collections.sort(temp);

            JSONArray newMasterItemsArray = new JSONArray();

            for (int i = 0; i < temp.size(); i++) {
                JSONObject newMasterListItem = new JSONObject().put("name", temp.get(i));
                newMasterItemsArray.put(newMasterListItem);
            }

            JSONObject newItemsMaster = new JSONObject().put("items", newMasterItemsArray);
            JSONObject locationJSONObj = new JSONObject();

            locationJSONObj.put("long", theList.getLocation().getLong());
            locationJSONObj.put("lat", theList.getLocation().getLat());
            locationJSONObj.put("address", theList.getLocation().getAddress());

            newItemsMaster.put("title", listName);

            newItemsMaster.put("location", locationJSONObj);

            Log.d(TAG, "Line " + Thread.currentThread().getStackTrace()[2].getLineNumber
                    () + " " + newItemsMaster.toString());

            FileOutputStream outputStream = new FileOutputStream(listFile);
            outputStream.write(newItemsMaster.toString().getBytes());
            outputStream.close();

        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a Dialog for user input. The user will either enter a new item or back out
     * of the Dialog.<br/>When the user inputs a new item and hits the "save" button,
     * the item is added to the list and then saved out to the file. The item is compared
     * to the other items in the list and added alphabetically.
     */
    private void addItem(){
        Log.d(TAG, "Line " + Thread.currentThread().getStackTrace()[2].getLineNumber
                () + " " + "adding item");
        final TodoItem item = new TodoItem();

        final Dialog itemDialog = new Dialog(this);
        itemDialog.setContentView(R.layout.custom_item_dialog);
        itemDialog.setTitle("Enter the new items's name:");

        final EditText editText = (EditText) itemDialog.findViewById(R.id.item_dialog_item_content);
        editText.setHint("Enter item name here");

        Button dialogButton = (Button) itemDialog.findViewById(R.id.item_dialog_item_button);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.getText().toString().equals("")) {
                    Toast.makeText(context, "Please enter a item name", Toast.LENGTH_SHORT).show();
                } else {
                    item.setItemName(editText.getText().toString());

                    Log.d(TAG, "Line " + Thread.currentThread().getStackTrace()[2].getLineNumber
                            () + " " + "item name: " + item.getItemName());

                    insertIntoList(item.getItemName());

                    int index = 0;

                    for (int i = 0; i < theList.listItems.size(); i++) {
                        TodoItem temp = (TodoItem) theList.listItems.get(i);
                        Log.d(TAG, "Line " + Thread.currentThread().getStackTrace()[2].getLineNumber
                                () + " " + temp.getItemName());

                        if (temp.getItemName().compareToIgnoreCase(item.getItemName()) < 0) {
                            index++;
                        } else {
                            break;
                        }
                    }

                    Log.d(TAG, "Line " + Thread.currentThread().getStackTrace()[2].getLineNumber
                            () + " " + "Add item at location: " + index);

                    theList.listItems.add(index, item);

                    itemAdapter.notifyItemInserted(theList.listItems.indexOf(item));
                    itemDialog.dismiss();
                }
            }
        });

        itemDialog.show();
    }

    /**
     * Grabs the old item out of the adapter, remove the item at the oldPos.
     * The item is then added at the newPos location and the adapter is notified.
     *
     * @param oldPos old index for the item
     * @param newPos new index for the item
     */
    private void moveItem(int oldPos, int newPos){
        TodoItem item = (TodoItem) theList.listItems.get(oldPos);
        theList.listItems.remove(oldPos);
        theList.listItems.add(newPos, item);
        itemAdapter.notifyItemMoved(oldPos, newPos);
    }

    /**
     * Calls deleteFromInternal and the removes the item from the adapter.
     *
     * @param pos position of the item to delete
     */
    private void deleteItem(final int pos){
        deleteFromInternal(pos);
        theList.listItems.remove(pos);
        itemAdapter.notifyItemRemoved(pos);
    }

    /**
     * Reads the file from the internal storage, populates a json object with the
     * contents of the file. The item in the "items" value at position "pos" is deleted
     * and the file is saved.
     *
     * @param pos position of item to be deleted
     */
    private void deleteFromInternal(int pos) {
        try {
            FileInputStream fileInputStream = new FileInputStream(listFile);
            byte[] bytes = new byte[(int) listFile.length()];

            fileInputStream.read(bytes);
            fileInputStream.close();

            String listJSONStr = new String(bytes);

            Log.d(TAG, "listJSONStr: " + listJSONStr);

            JSONObject listJSONObj = new JSONObject(listJSONStr);
            JSONArray listJSONItemsArray = listJSONObj.getJSONArray("items");

            listJSONItemsArray.remove(pos);
            listJSONObj.put("items", listJSONItemsArray);
            Log.d(TAG, "Added listJSONItemsArray: " + listJSONObj.toString());

            FileOutputStream fileOutputStream = new FileOutputStream(listFile);
            fileOutputStream.write(listJSONObj.toString().getBytes());
            fileOutputStream.close();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
}
