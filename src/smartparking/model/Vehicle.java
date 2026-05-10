/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package smartparking.model;

  public class Vehicle {
    private int vehicleId, userId;
    private String licensePlate;

    public Vehicle() {}
    public Vehicle(int vehicleId, String licensePlate, int userId) {
        this.vehicleId = vehicleId;
        this.licensePlate = licensePlate;
        this.userId = userId;
    }

    public int getVehicleId()               { return vehicleId; }
    public void setVehicleId(int v)         { vehicleId = v; }
    public String getLicensePlate()         { return licensePlate; }
    public void setLicensePlate(String v)   { licensePlate = v; }
    public int getUserId()                  { return userId; }
    public void setUserId(int v)            { userId = v; }

    @Override public String toString()      { return licensePlate; }
}
