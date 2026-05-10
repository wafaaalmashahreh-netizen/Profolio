package smartparking.model;

import java.sql.Date;
import java.sql.Time;

public class Reservation {
    private int reservationId, userId, vehicleId, spotId;
    private Date resDate;
    private Time startTime, endTime;
    private String status, userName, licensePlate, areaName;

    public Reservation() {}

    public int getReservationId()               { return reservationId; }
    public void setReservationId(int v)         { reservationId = v; }
    public int getUserId()                      { return userId; }
    public void setUserId(int v)                { userId = v; }
    public int getVehicleId()                   { return vehicleId; }
    public void setVehicleId(int v)             { vehicleId = v; }
    public int getSpotId()                      { return spotId; }
    public void setSpotId(int v)                { spotId = v; }
    public Date getResDate()                    { return resDate; }
    public void setResDate(Date v)              { resDate = v; }
    public Time getStartTime()                  { return startTime; }
    public void setStartTime(Time v)            { startTime = v; }
    public Time getEndTime()                    { return endTime; }
    public void setEndTime(Time v)              { endTime = v; }
    public String getStatus()                   { return status; }
    public void setStatus(String v)             { status = v; }
    public String getUserName()                 { return userName; }
    public void setUserName(String v)           { userName = v; }
    public String getLicensePlate()             { return licensePlate; }
    public void setLicensePlate(String v)       { licensePlate = v; }
    public String getAreaName()                 { return areaName; }
    public void setAreaName(String v)           { areaName = v; }
}