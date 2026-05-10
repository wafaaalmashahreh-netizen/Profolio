/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package smartparking.model;


 public class ParkingArea {
    private int areaId;
    private String name, allowedFor, prefix;

    public int getAreaId()              { return areaId; }
    public void setAreaId(int v)        { areaId = v; }
    public String getName()             { return name; }
    public void setName(String v)       { name = v; }
    public String getAllowedFor()       { return allowedFor; }
    public void setAllowedFor(String v) { allowedFor = v; }
    public String getPrefix()           { return prefix; }
    public void setPrefix(String v)     { prefix = v; }

    @Override public String toString()  { return areaId + " - " + name; }
}
