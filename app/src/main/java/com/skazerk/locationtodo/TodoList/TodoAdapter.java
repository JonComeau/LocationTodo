package com.skazerk.locationtodo.TodoList;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.skazerk.locationtodo.R;

import java.util.List;

/**
 * Created by Skaze on 6/1/17.
 */

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.ViewHolder> {

    private List<TodoItem> todoItemList;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public CheckBox checkBox;

        public ViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.item_name);
        }
    }

    public TodoAdapter(List<TodoItem> todoList){

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
