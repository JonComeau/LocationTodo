package com.skazerk.locationtodo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.skazerk.locationtodo.R;
import com.skazerk.locationtodo.model.TodoItem;
import com.skazerk.locationtodo.model.TodoList;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Garrett on 6/28/2017.
 */

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ListHolder>{

    private TodoList itemData;
    private LayoutInflater inflater;

    public MyAdapter(TodoList itemList, Context context){
        this.inflater = LayoutInflater.from(context);
        this.itemData = itemList;
    }

    @Override
    public ListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_layout, parent, false);
        return new ListHolder(view);
    }

    @Override
    public void onBindViewHolder(ListHolder holder, int position) {
        TodoItem item = itemData.listItems.get(position);
        holder.itemName.setText(item.getItemName());
    }

    @Override
    public int getItemCount() {
        return itemData.listItems.size();
    }

    class ListHolder extends RecyclerView.ViewHolder{

        private TextView itemName;
        private View container;

        public ListHolder(View itemView) {
            super(itemView);

            itemName = (TextView)itemView.findViewById(R.id.todo_item);
            container = itemView.findViewById(R.id.item_container);
        }
    }
}













