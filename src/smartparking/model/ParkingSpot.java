/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package smartparking.model;

 public class ParkingSpot {
    private int spotId, areaId;
    private String areaName, colorStatus, spotLabel;

    public ParkingSpot() {}

    public int getSpotId()                  { return spotId; }
    public void setSpotId(int v)            { spotId = v; }
    public int getAreaId()                  { return areaId; }
    public void setAreaId(int v)            { areaId = v; }
    public String getAreaName()             { return areaName; }
    public void setAreaName(String v)       { areaName = v; }
    public String getColorStatus()          { return colorStatus; }
    public void setColorStatus(String v)    { colorStatus = v; }
    public String getSpotLabel()            { return spotLabel != null ? spotLabel : "P" + spotId; }
    public void setSpotLabel(String v)      { spotLabel = v; }
    public boolean isAvailable()            { return "green".equals(colorStatus); }

    @Override public String toString()      { return getSpotLabel() + (isAvailable() ? " - Free" : " - Taken"); }}