package com.example.harish.geomindr.fragment.main;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.harish.geomindr.R;
import com.example.harish.geomindr.activity.ebr.EntityBasedReminderActivity;
import com.example.harish.geomindr.activity.reminder.view.Reminder;
import com.example.harish.geomindr.activity.reminder.view.ReminderRecyclerAdapter;
import com.example.harish.geomindr.activity.tbr.alarm.AlarmTask;
import com.example.harish.geomindr.activity.tbr.message.MessageTask;
import com.example.harish.geomindr.database.DatabaseHelper;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.util.List;

import static android.widget.Toast.makeText;
import static com.example.harish.geomindr.activity.reminder.view.ReminderRecyclerAdapter.reminderList;

public class HomeFragment extends Fragment implements RecyclerView.OnItemTouchListener, View.OnClickListener{
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 1;
    Context mContext;
    ActionBar mActionBar;
    RecyclerView taskReminderList;
    ReminderRecyclerAdapter taskReminderListAdapter;

    // Action Mode specific members
    ActionMode taskReminderListActionMode;
    ActionMode.Callback taskReminderListActionModeCallback;
    GestureDetectorCompat taskReminderListGestureDetector;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_home, container, false);

        getActivity().getWindow().setStatusBarColor(ContextCompat.
            getColor(getContext(), R.color.colorPrimaryDark));

        // Setting the FloatingActionMenu and FloatingActionButtons.
        final FloatingActionMenu addReminderFAM = (FloatingActionMenu)
                view.findViewById(R.id.add_reminder);
        final FloatingActionButton fabTBR = (FloatingActionButton)
                view.findViewById(R.id.fab_tbr);
        final FloatingActionButton fabEBR = (FloatingActionButton)
                view.findViewById(R.id.fab_ebr);
        final FloatingActionButton fabTBRAlarm = new FloatingActionButton(getContext());
        final FloatingActionButton fabTBRMessage = new FloatingActionButton(getContext());

        mContext = getActivity().getApplicationContext();

        // Get action bar
        mActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        // RecyclerViewObject.
        taskReminderList = (RecyclerView) view.findViewById(R.id.reminder_list);

        // Action Mode Objects
        taskReminderListGestureDetector = new GestureDetectorCompat(getContext(),
                new TaskReminderListOnGestureListener());
        taskReminderListActionModeCallback =
            new ActionMode.Callback() {
                int statusBarColor;

                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    MenuInflater inflater = mode.getMenuInflater();
                    inflater.inflate(R.menu.action_mode_options_menu, menu);

                    // hide FAM
                    if (addReminderFAM.isOpened())
                        addReminderFAM.close(true);
                    addReminderFAM.setVisibility(View.GONE);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        //hold current color of status bar
                        statusBarColor = getActivity().getWindow().getStatusBarColor();
                        //set your color
                        getActivity().getWindow()
                                .setStatusBarColor(ContextCompat
                                        .getColor(mContext, R.color.colorPrimaryDark));

                    }

                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    mode.setTitle("Delete Reminder");
                    return false;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    List<Integer> selectedItemPositions
                            = taskReminderListAdapter.getSelectedItems();
                    int currPos = -1;
                    boolean flag = false;
                    DatabaseHelper databaseHelper = DatabaseHelper.getInstance(getContext());

                    for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
                        currPos = selectedItemPositions.get(i);

                        if (currPos >= 0) {
                            Reminder tbr = reminderList.get(currPos);
                            databaseHelper.deleteTask(tbr.getReminderId());
                            reminderList.remove(currPos);
                            flag = true;
                        }
                        else {
                            Toast.makeText(mContext, "No reminder selected.", Toast.LENGTH_SHORT).show();
                            return false;
                        }
                    }

                    if (flag) {
                        makeText(getContext(), "Reminder deleted.", Toast.LENGTH_SHORT).show();
                        taskReminderListAdapter.notifyDataSetChanged();
                        mode.finish();
                    }
                    else {
                        Toast.makeText(mContext, "No reminder selected.", Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    return true;
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
                    taskReminderListActionMode = null;
                    taskReminderListAdapter.clearSelections();

                    addReminderFAM.setVisibility(View.VISIBLE);
                }
            };

        // Setting the 'Alarm Task' FloatingActionButton programmatically
        // because we will show it only if user clicks on 'Task Based Reminder' FloatingActionButton.
        fabTBRAlarm.setImageResource(R.drawable.ic_fab_alarm);
        fabTBRAlarm.setButtonSize(FloatingActionButton.SIZE_MINI);
        fabTBRAlarm.setLabelText("Alarm Task");
        fabTBRAlarm.setColorNormalResId(R.color.colorPrimaryDark);
        fabTBRAlarm.setColorPressedResId(R.color.colorPrimaryDark);
        fabTBRAlarm.setColorRippleResId(R.color.colorPrimaryDark);

        // Setting the 'Message Task' FloatingActionButton programmatically
        // because we will show it only if user clicks on 'Task Based Reminder' FloatingActionButton.
        fabTBRMessage.setImageResource(R.drawable.ic_textsms_white_24dp);
        fabTBRMessage.setButtonSize(FloatingActionButton.SIZE_MINI);
        fabTBRMessage.setLabelText("Message Task");
        fabTBRMessage.setColorNormalResId(R.color.colorPrimaryDark);
        fabTBRMessage.setColorPressedResId(R.color.colorPrimaryDark);
        fabTBRMessage.setColorRippleResId(R.color.colorPrimaryDark);

        // If the 'Task Based Reminder' FloatingActionButton is clicked, then
        // add or remove the above 3 buttons programmatically.
        fabTBR.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (fabTBRAlarm.getParent() == null
                        && fabTBRMessage.getParent() == null) {
                    addReminderFAM.addMenuButton(fabTBRAlarm, 0);
                    addReminderFAM.addMenuButton(fabTBRMessage, 1);
                }
                else {
                    addReminderFAM.removeMenuButton(fabTBRAlarm);
                    addReminderFAM.removeMenuButton(fabTBRMessage);
                }
            }
        });

        fabEBR.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(getContext(), EntityBasedReminderActivity.class));
            }
        });

        // Start 'AlarmTask' activity to add a new alarm task reminder.
        fabTBRAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), AlarmTask.class);
                startActivity(intent);
            }
        });

        // Start 'MessageTask' activity to add a new message task reminder.
        fabTBRMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // First, check for send SMS permission.
                // Proceed only if user grants the permission.
                int permissionCheck = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.SEND_SMS);

                // If permission is not granted, prompt user for permission.
                if(permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.SEND_SMS},
                            MY_PERMISSIONS_REQUEST_SEND_SMS);
                }
                // Else, start the desired activity.
                else {
                    Intent intent = new Intent(getContext(), MessageTask.class);
                    startActivity(intent);
                }
            }
        });

        // Setting up the RecyclerView with reminders present in the database.
        taskReminderListAdapter = new ReminderRecyclerAdapter(getContext());
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        taskReminderList.setLayoutManager(mLayoutManager);
        taskReminderList.setItemAnimator(new DefaultItemAnimator());
        taskReminderList.setAdapter(taskReminderListAdapter);
        taskReminderList.addOnItemTouchListener(this);
        taskReminderListAdapter.notifyDataSetChanged();

        return view;
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        taskReminderListGestureDetector.onTouchEvent(e);
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    @Override
    public void onClick(View v) {
        if (v == null) {
            int selectCount;
            taskReminderListAdapter.clearSelections();
            selectCount = taskReminderListAdapter.getSelectedItemCount();
            if (selectCount == 0 && taskReminderListActionMode != null) {
                taskReminderListActionMode.finish();
                taskReminderListActionMode = null;
            }
            return;
        }

        switch (v.getId()) {
            case R.id.rem_list_container_item:
                int idx = taskReminderList.getChildAdapterPosition(v);
                if (taskReminderListActionMode != null) {
                    int selectCount;
                    myToggleSelection(idx);
                    selectCount = taskReminderListAdapter.getSelectedItemCount();
                    if (selectCount == 0) {
                        taskReminderListActionMode.finish();
                        taskReminderListActionMode = null;
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                // If the user has granted the permission, then proceed and open the desired activity
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(getContext(), MessageTask.class);
                    startActivity(intent);
                }
            }
            break;
        }
    }

    private void myToggleSelection(int idx) {
        taskReminderListAdapter.toggleSelection(idx);
    }

    private class TaskReminderListOnGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            View view = taskReminderList.findChildViewUnder(e.getX(), e.getY());
            onClick(view);
            return super.onSingleTapConfirmed(e);
        }

        public void onLongPress(MotionEvent event) {
            View view = taskReminderList.findChildViewUnder(event.getX(), event.getY());

            if (taskReminderListActionMode != null) {
                return;
            }

            taskReminderListActionMode = ((AppCompatActivity) getActivity())
                    .startSupportActionMode(taskReminderListActionModeCallback);

            int idx = taskReminderList.getChildAdapterPosition(view);
            myToggleSelection(idx);

            super.onLongPress(event);
        }
    }
}