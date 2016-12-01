package com.example.harish.geomindr.activity.reminder.view;

import android.content.Context;
import android.database.Cursor;
import android.os.Vibrator;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.harish.geomindr.R;
import com.example.harish.geomindr.database.DatabaseHelper;
import com.github.clans.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ReminderRecyclerAdapter extends RecyclerView.Adapter<ReminderRecyclerAdapter.ViewHolder> {
    // List containing all the reminder in the database.
    public static List<Reminder> reminderList;
    // Context from which the class 'ReminderRecyclerAdapter' is called.
    protected Context context;
    private SparseBooleanArray selectedRecyclerViewItems;
    private Vibrator vibratorService;

    public ReminderRecyclerAdapter(Context context) {
        this.context = context;
        this.selectedRecyclerViewItems = new SparseBooleanArray();

        // To vibrate when user long clicks on a reminder tile.
        vibratorService = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        reminderList = new ArrayList<>();

        // Creating an object of DatabaseHelper class.
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);

        // Retrieving all records from the database.
        Cursor res = databaseHelper.getAllRecordsTBR();

        // Iterating through the retrieved records.
        while(res.moveToNext()) {
            // Add the data to the 'reminderList' list.
            reminderList.add(new Reminder(res.getInt(0), res.getInt(1), res.getInt(2),
                    res.getString(3), res.getString(4), res.getString(5), res.getString(6),
                    res.getString(7), res.getString(8), res.getDouble(9), res.getDouble(10)));
        }

        res.close();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.reminder_view, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Reminder reminder = reminderList.get(position);
        holder.title.setText(reminder.getTitle());
        holder.time.setText(reminder.getLocation());
        holder.description.setText(reminder.getDescription());

        // Set custom image on the FloatingActionButton on CardView displaying the reminder
        // If taskID = 1, then it is a facebook task reminder. So, set the image accordingly.
        if (reminder.getTaskId() == 1) {
            holder.mFABIcon.setImageResource(R.drawable.ic_fab_facebook);
        }
        // If taskID = 2, then it is a alarm task reminder. So, set the image accordingly.
        else if (reminder.getTaskId() == 2) {
            holder.mFABIcon.setImageResource(R.drawable.ic_alarm_white_24dp);
        }
        // If taskID = 3 or taskId = 5, then it is a message task reminder. So, set the image accordingly.
        else if (reminder.getTaskId() == 3 || reminder.getTaskId() == 5) {
            holder.mFABIcon.setImageResource(R.drawable.ic_textsms_white_24dp);
        }

        holder.itemView.setActivated(selectedRecyclerViewItems.get(position, false));
    }

    @Override
    public int getItemCount() {
        return reminderList.size();
    }

    // Methods for implementing Selection using contextActionMode

    public void toggleSelection(int position) {
        if (position >= 0) {
            if (selectedRecyclerViewItems.get(position, false)) {
                selectedRecyclerViewItems.delete(position);
            } else {
                vibratorService.vibrate(50);
                if (reminderList.get(position).getTaskId() == 3) {
                    selectedRecyclerViewItems.put(position, true);
                    selectedRecyclerViewItems.put(position + 1, true);
                } else if (reminderList.get(position).getTaskId() == 5) {
                    selectedRecyclerViewItems.put(position, true);
                    selectedRecyclerViewItems.put(position - 1, true);
                } else {
                    selectedRecyclerViewItems.put(position, true);
                }
            }
        }
        else {
            vibratorService.vibrate(50);
        }

        notifyItemChanged(position);
    }

    public void clearSelections() {
        selectedRecyclerViewItems.clear();
        notifyDataSetChanged();
    }

    public int getSelectedItemCount() {
        return selectedRecyclerViewItems.size();
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<>(selectedRecyclerViewItems.size());
        for (int i = 0; i < selectedRecyclerViewItems.size(); i++) {
            items.add(selectedRecyclerViewItems.keyAt(i));
        }

        return items;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView title, time, description;
        private FloatingActionButton mFABIcon;

        ViewHolder(View view) {
            super(view);

            mFABIcon = (FloatingActionButton) view.findViewById(R.id.fab_icon);
            title = (TextView) view.findViewById(R.id.rem_title);
            time = (TextView) view.findViewById(R.id.rem_time);
            description = (TextView) view.findViewById(R.id.rem_description);
        }
    }
}