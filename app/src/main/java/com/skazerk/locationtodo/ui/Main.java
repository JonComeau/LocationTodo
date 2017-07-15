package com.skazerk.locationtodo.ui;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Canvas;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.skazerk.locationtodo.Location.MyLocationService;
import com.skazerk.locationtodo.R;
import com.skazerk.locationtodo.adapter.MyListAdapter;
import com.skazerk.locationtodo.model.DerpData;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Garrett on 6/29/2017.
 */

public class Main extends AppCompatActivity implements MyListAdapter.ItemClickCallback{

    private static final String TAG = "MAIN";
    private final Context context  = this;

    public static final String EXTRA_LISTFILE = "EXTRA_LISTFILE";

    MyLocationService boundService;
    boolean serviceBound = false;

    private RecyclerView masterView;
    private MyListAdapter listAdapter;
    private ArrayList<TodoList> masterList;

    private String fileName;
    private File master;

    JSONObject masterJSONOject;
    JSONObject locationJSONObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        fileName = "master.json";

        master = new File(context.getFilesDir(), fileName);

        try {
            if (master.createNewFile()) {
                masterJSONOject = new JSONObject();

                FileOutputStream outputStream = new FileOutputStream(master);
                outputStream.write(("{\"master\":[]}").getBytes());
                outputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        masterList = grabJSONData();

        grabLocationJSONData();

        masterView = (RecyclerView) findViewById(R.id.masterList);
        masterView.setLayoutManager(new LinearLayoutManager(this));
        listAdapter = new MyListAdapter(masterList, this);
        masterView.setAdapter(listAdapter);
        listAdapter.setListClickCallback(this);

        listAdapter.setUndo(true);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(createHelperCallback());
        itemTouchHelper.attachToRecyclerView(masterView);

        FloatingActionButton addList = (FloatingActionButton) findViewById(R.id.add_list);
        addList.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                addList();
            }
        });
    }

    /**
     * Creates an intent for a background service that monitors the users location
     * in relation to the saved locations.
     */
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, MyLocationService.class);
        intent.putExtra("JSON", locationJSONObj.toString());
        startService(intent);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * Kills the service when the app is destroyed
     */
    protected void onDestroy() {
        super.onDestroy();

        stopService(new Intent(this, MyLocationService.class));
        unbindService(serviceConnection);
    }

    /**
     * This function reads in the master file called "master.json", and saves the
     * content to a json object. The array value of "master" is read into an arraylist.
     * The new item from the parameter "name" is then added to the array and the array
     * is sorted using the Collections class. the resulting array is written back to the
     * file and saved.<br/>The name of the new list is used to create a new json file
     * with preset key:value pairs that will be used to hold the list's data.
     *
     * @param name name of the new list
     */
    private void insertIntoMaster(String name) {
        List<String> temp = new ArrayList<>();

        FileInputStream inputStream = null;
        byte[] bytes = new byte[(int) master.length()];
        String masterJSONStr;

        try {
            inputStream = new FileInputStream(master);
            inputStream.read(bytes);
            inputStream.close();
            masterJSONStr = new String(bytes);

            JSONObject masterJSONList = new JSONObject(masterJSONStr);
            JSONArray masterJSONItems = masterJSONList.getJSONArray("master");

            for (int i = 0; i < masterJSONItems.length(); i++) {
                JSONObject JSONItem = masterJSONItems.getJSONObject(i);
                temp.add(JSONItem.getString("name"));
            }

            temp.add(name);

            Collections.sort(temp);

            JSONArray newMasterArray = new JSONArray();

            for (int i = 0; i < temp.size(); i++) {
                JSONObject newMasterListItem = new JSONObject().put("name", temp.get(i));
                newMasterArray.put(newMasterListItem);
            }

            JSONObject newMaster = new JSONObject().put("master", newMasterArray);

            Log.d(TAG, newMaster.toString());

            FileOutputStream outputStream = new FileOutputStream(master);
            outputStream.write(newMaster.toString().getBytes());
            outputStream.close();

            String listFileName = name.toLowerCase() + ".json";
            File list = new File(context.getFilesDir(), listFileName);
            if (list.createNewFile()) {
                FileOutputStream listOutputStream = new FileOutputStream(list);
                listOutputStream.write(
                        (
                                "{" +
                                        "\"title\":\"" + name + "\"," +
                                        "\"location\":{" +
                                        "\"long\":\"0\"," +
                                        "\"lat\":\"0\"," +
                                        "\"address\":\"\"}," +
                                        "\"items\":[]" +
                                        "}"
                        ).getBytes()
                );
                listOutputStream.close();
            }

        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * "master.json" is read into a json object and parsed to grab the data about
     * each of the lists. "master.json" only hold the names for all the lists. Then, each
     * list's json file is read into the TodoList object associated with the list.
     * This data is saved in the program to populate the item lists for each list.
     *
     * @return arraylist of the master.json array values
     */
    private ArrayList grabJSONData() {
        Log.d(TAG, "grabbing data");
        ArrayList temp = new ArrayList();

        FileInputStream inputStream = null;
        byte[] bytes = new byte[(int) master.length()];
        String masterJSONStr;
        try {
            inputStream = new FileInputStream(master);
            inputStream.read(bytes);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        masterJSONStr = new String(bytes);

        Log.d(TAG, "opened master.json");
        Log.d(TAG, "grabbed JSONStr");
        Log.d(TAG, masterJSONStr);

        try {
            JSONObject masterJSON = new JSONObject(masterJSONStr);
            JSONArray masterJSONArray = masterJSON.getJSONArray("master");
            Log.d(TAG, "got masterArray");
            Log.d(TAG, "masterJSONArray.length(): " + masterJSONArray.length());

            for (int i = 0; i < masterJSONArray.length(); i++) {
                Log.d(TAG, "i: " + i);
                TodoList list = new TodoList();
                JSONObject masterListObj = masterJSONArray.getJSONObject(i);

                list.setListName(masterListObj.getString("name"));
                Log.d(TAG, "setting ListName: " + masterListObj.getString("name"));

                String newListFileName = masterListObj.getString("name").toLowerCase() + ".json";
                File newList = new File(context.getFilesDir(), newListFileName);

                FileInputStream listInput = new FileInputStream(newList);
                byte[] listBytes = new byte[(int) newList.length()];
                listInput.read(listBytes);
                listInput.close();

                String listJSONStr = new String(listBytes);

                Log.d(TAG, "grabbed: " + masterListObj.getString("name") + " JSON");
                Log.d(TAG, newList.getName() + " content: " + listJSONStr);

                JSONObject listObj = new JSONObject(listJSONStr);
                JSONObject locationObj = listObj.getJSONObject("location");

                list.setLocation(
                        ((locationObj.getString("long").equals("")) ? 0 : Float.parseFloat
                                (locationObj
                                        .getString("long"))),
                        ((locationObj.getString("lat").equals("")) ? 0 :Float.parseFloat
                                (locationObj
                                .getString("lat"))),
                        locationObj.getString("address")
                );
                Log.d(TAG, "set the Location");

                JSONArray itemsArray = listObj.getJSONArray("items");

                for(int j = 0; j < itemsArray.length(); j++) {
                    TodoItem item = new TodoItem();
                    JSONObject itemObj = itemsArray.getJSONObject(j);

                    item.setItemName(itemObj.getString("name"));
                    Log.d(TAG, "added " + itemObj.getString("name"));

                    list.listItems.add(item);
                }
                temp.add(list);
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }

        return temp;
    }

    /**
     * Read in the location values from each list file's json. This data is placed into
     * an array for delivery to the background service.
     */
    private void grabLocationJSONData() {
        locationJSONObj = new JSONObject();
        JSONArray locationJSONArray = new JSONArray();

        for (int i = 0; i < masterList.size(); i++) {
            JSONObject locationJSONItemObj = null;
            File file = new File(context.getFilesDir(), masterList.get(i).getListName()
                    .toLowerCase() + ".json");

            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                byte[] bytes = new byte[(int) file.length()];

                fileInputStream.read(bytes);
                fileInputStream.close();

                String listJSONStr = new String(bytes);

                Log.d(TAG, file.getName() + " JSON: " + listJSONStr);

                JSONObject tempLocation = new JSONObject(listJSONStr);

                locationJSONItemObj = tempLocation.getJSONObject("location");

                locationJSONArray.put(locationJSONItemObj);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            Log.d(TAG, masterList.get(i).getListName() + " loaded");
        }

        try {
            locationJSONObj.put("location", locationJSONArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * creates the event handlers for the recyclerview
     * <ul>
     *     <li>onMove: when an item is moved</li>
     *     <li>getSwipeDirs: returns the swipe direction, if undone, then 0</li>
     *     <li>onSwipe: when item is swiped, an undo button pops up, when pressed,
     *     undo the delete.</li>
     * </ul>
     *
     * @return the callback for the recyclerview
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
                    public int getSwipeDirs(RecyclerView recView, RecyclerView.ViewHolder vHolder){
                        int position = vHolder.getAdapterPosition();
                        MyListAdapter mLAdapter = (MyListAdapter) masterView.getAdapter();
                        if (mLAdapter.undoRemove() && mLAdapter.isRemovalPending(position))
                            return 0;
                        return super.getSwipeDirs(recView, vHolder);
                    }

                    @Override
                    public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                        Log.d(TAG, "Swipe");
                        int swipedIndex = viewHolder.getAdapterPosition();
                        MyListAdapter lAdapter = (MyListAdapter) masterView.getAdapter();
                        TodoList listToRemove = (TodoList) masterList.get(swipedIndex);
                        boolean undoIt = lAdapter.undoRemove();
                        if (undoIt) {
                            Log.d(TAG, "Undo");
                            lAdapter.removalPending(swipedIndex);
                        } else {
                            Log.d(TAG, "Swipe delete item");
                            //deleteList(viewHolder.getAdapterPosition());
                        }
                        if (!masterList.contains(listToRemove)) {
                            Log.d(TAG, "Swipe delete item master");
                            //deleteList(swipedIndex);
                        }
                    }
                };
        return simpleItemTouchCallback;
    }

    /**
     * create a dialog for the user to input a new list. when the user hits save,
     * the list is added to the master list, both in the adapter and the file. The file
     * is read in, the item is added, the the file is saved with the change.
     */
    private void addList(){
        Log.d(TAG, "adding list");
        final TodoList list = new TodoList();

        final Dialog itemDialog = new Dialog(this);
        itemDialog.setContentView(R.layout.custom_list_dialog);
        itemDialog.setTitle("Enter the new list's title:");

        final EditText editText = (EditText) itemDialog.findViewById(R.id.list_dialog_item_content);
        editText.setHint("Enter list title here");

        Button dialogButton = (Button) itemDialog.findViewById(R.id.list_dialog_item_button);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.getText().toString().equals("")) {
                    Toast.makeText(context, "Please enter a list Title", Toast.LENGTH_SHORT).show();
                } else {
                    String title = editText.getText().toString();

                    list.setListName(title);

                    Log.d(TAG, "list name: " + list.getListName());

                    insertIntoMaster(list.getListName());

                    int index = 0;

                    for (int i = 0; i < masterList.size(); i++) {
                        TodoList temp = (TodoList) masterList.get(i);
                        Log.d(TAG, temp.getListName());

                        if (temp.getListName().compareToIgnoreCase(list.getListName()) < 0) {
                            index++;
                        } else {
                            break;
                        }
                    }

                    Log.d(TAG, "Add list at location: " + index);

                    masterList.add(list);

                    listAdapter.notifyItemInserted(masterList.indexOf(list));
                    itemDialog.dismiss();
                }
            }
        });

        itemDialog.show();
    }

    private void moveItem(int oldPos, int newPos){
        TodoList list = (TodoList) masterList.get(oldPos);
        masterList.remove(oldPos);
        masterList.add(newPos, list);
        listAdapter.notifyItemMoved(oldPos, newPos);
    }

    @Override
    public void onListClick(int p) {
        TodoList list = (TodoList) masterList.get(p);
        String listFileName = list.getListName();

        Intent i = new Intent(this, ListActivity.class);
        i.putExtra(EXTRA_LISTFILE, listFileName);
        startActivity(i);
    }

    /**
     * This is an empty function so we can implement the itemClickCallback from our
     * custom adapter.
     *
     * @param p position of the option
     */
    @Override
    public void onOptionClick(int p) {

    }

    /**
     * Binds the background service to the activity.
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyLocationService.MyBinder myBinder = (MyLocationService.MyBinder) service;
            boundService = myBinder.getService();
            serviceBound = true;
        }
    };
}

