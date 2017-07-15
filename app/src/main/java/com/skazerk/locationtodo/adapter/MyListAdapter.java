package com.skazerk.locationtodo.adapter;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.skazerk.locationtodo.R;
import com.skazerk.locationtodo.model.TodoList;
import com.skazerk.locationtodo.ui.Main;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Garrett on 6/28/2017.
 */

public class MyListAdapter extends RecyclerView.Adapter<MyListAdapter.ListHolder>{

    private static final String TAG = "MY_LIST_ADAPTER";
    private ArrayList<TodoList> listData;
    ArrayList<TodoList> listsToRemove;
    private LayoutInflater inflater;
    Context context;
    boolean undo;
    private Handler handler = new Handler();
    HashMap<TodoList, Runnable> pendingRunnables = new HashMap<>();
    private static final int PENDING_REMOVAL_TIMEOUT = 4000;

    ItemClickCallback listClickCallback;

    public interface ItemClickCallback{
        void onListClick(int p);
        void onOptionClick(int p);
    }

    public void setListClickCallback(final ItemClickCallback itemClickCallback){
        this.listClickCallback = itemClickCallback;
    }

    public MyListAdapter(ArrayList<TodoList> listData, Context context){
        this.context = context;
        this.listData = listData;
        listsToRemove = new ArrayList<>();
    }

    @Override
    public ListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_layout, parent, false);
        ListHolder holder = new ListHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ListHolder holder, int position) {
        final TodoList list = listData.get(position);
        holder.listName.setText(list.getListName());

        if(listsToRemove.contains(list)){
            holder.listName.setVisibility(View.GONE);
            holder.undoButton.setVisibility(View.VISIBLE);
            holder.undoButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    Runnable pendingRunnable = pendingRunnables.get(list);
                    pendingRunnables.remove(list);
                    if(pendingRunnable != null)
                        handler.removeCallbacks(pendingRunnable);
                    listsToRemove.remove(list);
                    notifyItemChanged(listData.indexOf(list));
                }
            });
        } else{
            holder.listName.setVisibility(View.VISIBLE);
            holder.undoButton.setVisibility(View.GONE);
            holder.undoButton.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    class ListHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView listName;
        private View container;
        Button undoButton;

        public ListHolder(View itemView) {
            super(itemView);

            listName = (TextView)itemView.findViewById(R.id.list_name);
            listName.setOnClickListener(this);
            container = itemView.findViewById(R.id.list_container);
            container.setOnClickListener(this);
            undoButton = (Button) itemView.findViewById(R.id.undo_button);
        }

        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.list_container || v.getId() == R.id.list_name){
                listClickCallback.onListClick(getAdapterPosition());
            } else {
                listClickCallback.onOptionClick(getAdapterPosition());
            }
        }
    }

    public boolean undoRemove(){
        return undo;
    }

    public void setUndo(boolean undoIt){
        this.undo = undoIt;
    }

    // undo swiping idea from this site:
    // http://nemanjakovacevic.net/blog/english/2016/01/12/recyclerview-swipe-to-delete-no-3rd-party-lib-necessary/
    public void removalPending(int pos){
        final TodoList temp = listData.get(pos);
        if(!listsToRemove.contains(temp)){
            listsToRemove.add(temp);
            notifyItemChanged(pos);
            final int position = pos;
            Runnable pendingRun = new Runnable(){
                @Override
                public void run(){
                    TodoList mlist = listData.get(position);
                    deleteList(position, mlist);
                    if(listsToRemove.contains(mlist)){
                        listsToRemove.remove(mlist);
                    }
                    if(listData.contains(mlist)){
                        listData.remove(position);
                        notifyItemRemoved(position);
                    }
                }
            };
            handler.postDelayed(pendingRun, PENDING_REMOVAL_TIMEOUT);
            pendingRunnables.put(temp,pendingRun);
        }
    }

    public void deleteList(final int pos, TodoList list){
        Log.d(TAG, "Delete List");
        String fileName = list.getListName() + ".json";

        try {
            File master = new File(context.getFilesDir(), "master.json");

            FileInputStream input = new FileInputStream(master);
            byte[] bytes = new byte[(int) master.length()];
            input.read(bytes);
            input.close();

            String masterJSONStr = new String(bytes);

            JSONObject masterJSONObj = new JSONObject(masterJSONStr);
            JSONArray masterJSONArray = masterJSONObj.getJSONArray("master");

            Log.d(TAG, "Before: " + masterJSONObj.toString());

            masterJSONArray.remove(pos);

            Log.d(TAG, "After: " + masterJSONObj.toString());

            FileOutputStream output = new FileOutputStream(master);
            output.write(masterJSONObj.toString().getBytes());
            output.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        File file = new File(context.getFilesDir(), fileName);
        if(file.exists()) {
            context.deleteFile(fileName);
            Toast.makeText(context, "List deleted", Toast.LENGTH_SHORT).show();
        }

        notifyItemRemoved(pos);
    }

    public boolean isRemovalPending(int pos){
        TodoList temp = listData.get(pos);
        return listsToRemove.contains(temp);
    }
}







