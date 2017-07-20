package com.sargent.mark.todolist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.sargent.mark.todolist.data.Contract;

/**
 * Created by mark on 7/4/17.
 */

public class ToDoListAdapter extends RecyclerView.Adapter<ToDoListAdapter.ItemHolder> {

    private Cursor cursor;
    private ItemClickListener listener;
    private String TAG = "todolistadapter";

    //need db to do database commands on the fly
    private SQLiteDatabase db;

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.item, parent, false);
        ItemHolder holder = new ItemHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ItemHolder holder, int position) {
        holder.bind(holder, position);
    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }

    public interface ItemClickListener {
        //take in category too
        void onItemClick(int pos, String description, String duedate, long id, String category);
    }

    public ToDoListAdapter(Cursor cursor, ItemClickListener listener, SQLiteDatabase db) {
        this.cursor = cursor;
        this.listener = listener;

        //store the db to change too
        this.db = db;
    }

    public void swapCursor(Cursor newCursor){
        if (cursor != null) cursor.close();
        cursor = newCursor;
        if (newCursor != null) {
            // Force the RecyclerView to refresh
            this.notifyDataSetChanged();
        }
    }

    class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView descr;
        TextView due;
        String duedate;
        String description;
        long id;

        //hold category and completion box too
        String category;
        CheckBox checkBox;

        //in case we need to change how view looks
        View view;

        ItemHolder(View view) {
            super(view);
            descr = (TextView) view.findViewById(R.id.description);
            due = (TextView) view.findViewById(R.id.dueDate);

            //define checkbox too
            checkBox = (CheckBox) view.findViewById(R.id.checkBox);

            //view updater
            this.view = view;

            view.setOnClickListener(this);
        }

        public void bind(ItemHolder holder, int pos) {
            cursor.moveToPosition(pos);
            id = cursor.getLong(cursor.getColumnIndex(Contract.TABLE_TODO._ID));
            Log.d(TAG, "deleting id: " + id);

            duedate = cursor.getString(cursor.getColumnIndex(Contract.TABLE_TODO.COLUMN_NAME_DUE_DATE));
            description = cursor.getString(cursor.getColumnIndex(Contract.TABLE_TODO.COLUMN_NAME_DESCRIPTION));
            descr.setText(description);
            due.setText(duedate);
            holder.itemView.setTag(id);

            //store in category too
            category = cursor.getString(cursor.getColumnIndex(Contract.TABLE_TODO.COLUMN_NAME_CATEGORY));


            //display check completion too
            if(cursor.getInt(cursor.getColumnIndex(Contract.TABLE_TODO.COLUMN_NAME_COMPLETION)) == 1){
                checkBox.setChecked(true);
            }
            else{
                checkBox.setChecked(false);
            }

            //checkbox listener will update database
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(checkBox.isChecked()) {
                        isCompleted(true);
                    }
                    else{
                        isCompleted(false);
                    }
                }
            });
        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            //take in category too
            listener.onItemClick(pos, description, duedate, id, category);
        }

        //Function will put in content values into database
        public void isCompleted(boolean completed){
            ContentValues cv = new ContentValues();

            if(completed) {
                cv.put(Contract.TABLE_TODO.COLUMN_NAME_COMPLETION, 1);
            }
            else{
                cv.put(Contract.TABLE_TODO.COLUMN_NAME_COMPLETION, 0);
            }
            db.update(Contract.TABLE_TODO.TABLE_NAME, cv, Contract.TABLE_TODO._ID + "=" + id, null);
        }
    }

}
