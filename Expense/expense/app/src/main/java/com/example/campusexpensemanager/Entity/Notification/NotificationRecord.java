package com.example.campusexpensemanager.Entity.Notification;
import java.util.Objects;
public class NotificationRecord {
    private String userId;
    private String actionType;  // e.g., "Add", "Delete"
    private String expenseId;
    private String dateTime;
    private String description;
    // Constructor for "Add" action type
    public NotificationRecord(String description, String dateTime) {
        this.actionType = "Add";
        this.dateTime = dateTime;
        this.description = description;
    }
    // Constructor for "Delete" action type
    public NotificationRecord(String description, String dateTime, boolean isDelete) {
        if (isDelete) {
            this.actionType = "Delete";
        } else {
            this.actionType = "Add";  // This might be unnecessary if you always use this constructor for delete actions.
        }
        this.dateTime = dateTime;
        this.description = description;
    }
    // Full constructor for cases where all information is available
    public NotificationRecord(String userId, String actionType, String expenseId, String dateTime, String description) {
        this.userId = userId;
        this.actionType = actionType;
        this.expenseId = expenseId;
        this.dateTime = dateTime;
        this.description = description;
    }
    // Getters
    public String getUserId() {
        return userId;
    }
    public String getActionType() {
        return actionType;
    }
    public String getExpenseId() {
        return expenseId;
    }
    public String getDateTime() {
        return dateTime;
    }
    public String getDescription() {
        return description;
    }
    // Convert object to a string for storage
    @Override
    public String toString() {
        return userId + ";" + actionType + ";" + expenseId + ";" + dateTime + ";" + description;
    }
    // Convert stored string data back to an object
    public static NotificationRecord fromString(String notificationString) {
        String[] parts = notificationString.split(";");
        String userId = parts[0];
        String actionType = parts[1];
        String expenseId = parts.length > 2 ? parts[2] : "";
        String dateTime = parts.length > 3 ? parts[3] : "";
        String description = parts.length > 4 ? parts[4] : "";
        return new NotificationRecord(userId, actionType, expenseId, dateTime, description);
    }
    // Override equals and hashCode for comparison
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        NotificationRecord that = (NotificationRecord) obj;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(actionType, that.actionType) &&
                Objects.equals(expenseId, that.expenseId) &&
                Objects.equals(dateTime, that.dateTime);
    }
    @Override
    public int hashCode() {
        return Objects.hash(userId, actionType, expenseId, dateTime);
    }
}