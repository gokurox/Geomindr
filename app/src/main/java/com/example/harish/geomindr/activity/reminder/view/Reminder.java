package com.example.harish.geomindr.activity.reminder.view;

// Class representing the reminder created by the user.
public class Reminder {
    // Unique ID for every reminder created by the user.
    private int reminderId;
    // ID based on the task performed by the reminder.
    // ID is 1 for facebook task.
    // ID is 2 for alarm task.
    // ID is 3 for arrival message task.
    // ID is 4 for entity based task.
    // ID is 5 for departure message task.
    private int taskId;
    // Type of reminder
    // 1 means reminder is a 'Task Based Reminder'.
    // 2 means reminder is an 'Entity Based Reminder'.
    private int reminderType;
    // Title of the reminder.
    private String title;
    // Name of the person to whom user wants to send the message in message task reminder.
    private String name;
    // Contact number of the person to whom user wants to send the message in message task reminder.
    private String number;
    // Message upon arrival.
    private String arrivalMessage;
    // Message upon departure - used only in message task reminder.
    private String departureMessage;
    // Name of the location where reminder notification will be triggered.
    private String locationName;
    // Latitude of the location where reminder notification will be triggered.
    private double locationLatitude;
    // Longitude of the location where reminder notification will be triggered.
    private double locationLongitude;

    Reminder(int reminderId, int taskId, int reminderType, String title, String name,
             String number, String arrivalMessage, String departureMessage,
             String locationName, double locationLatitude, double locationLongitude) {
        this.reminderId = reminderId;
        this.taskId = taskId;
        this.reminderType = reminderType;
        this.title = title;
        this.name = name;
        this.number = number;
        this.arrivalMessage = arrivalMessage;
        this.departureMessage = departureMessage;
        this.locationName = locationName;
        this.locationLatitude = locationLatitude;
        this.locationLongitude = locationLongitude;
    }

    public String getTitle() {
        String title = this.title;

        if(this.taskId == 3 || this.taskId == 5) {
            title = "Message ";
            if(this.name != null) {
                title += this.name;
            }
            else {
                title += this.number;
            }
        }
        else if (this.taskId == 1) {
            title = "Post to Facebook";
        }

        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLocation() {
        String location = "\n";

        location += "at " + this.locationName;

        return location;
    }

    public int getReminderId() {
        return reminderId;
    }

    String getDescription() {
        String description = "\n";

        // if it is a facebook post task
        if(this.taskId == 1) {
            description += this.arrivalMessage;
        }
        // if it is a trigger alarm task
        else if(this.taskId == 2) {
            description += this.arrivalMessage;
        }
        // if it is a send message task - arrival
        else if(this.taskId == 3){
            description += "Upon arrival - " + this.arrivalMessage;
        }
        // if it is a send message task - departure
        else if(this.taskId == 5) {
            description += "Upon departure - " + this.departureMessage;
        }

        return description;
    }

    int getTaskId() {
        return this.taskId;
    }
}