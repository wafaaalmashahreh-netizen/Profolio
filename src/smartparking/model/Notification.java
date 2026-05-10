package smartparking.model;

import java.sql.Date;

public class Notification {
    private int notificationId;
    private int userId;
    private String message;
    private boolean isRead;
    private Date createdAt; // maps to column 'date' in DB

    public Notification() {}

    public int getNotificationId()           { return notificationId; }
    public void setNotificationId(int v)     { notificationId = v; }
    public int getUserId()                   { return userId; }
    public void setUserId(int v)             { userId = v; }
    public String getMessage()               { return message; }
    public void setMessage(String v)         { message = v; }
    public boolean isRead()                  { return isRead; }
    public void setRead(boolean v)           { isRead = v; }
    public Date getCreatedAt()               { return createdAt; }
    public void setCreatedAt(Date v)         { createdAt = v; }
}