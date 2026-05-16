package ec.unl.flota.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class VehicleState {

    @Id
    private String vehicleId;

    private String lastTimestamp;
    private double lastLat;
    private double lastLng;
    private double lastSpeed;
    private Double lastTemperature;
    private Double lastFuelLevel;
    private String lastAlert;
    private String lastAlertSeverity;
    private String lastAlertTimestamp;

    protected VehicleState() {
        // Constructor requerido por JPA.
    }

    public VehicleState(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public void updateGps(String timestamp, double lat, double lng, double speed) {
        this.lastTimestamp = timestamp;
        this.lastLat = lat;
        this.lastLng = lng;
        this.lastSpeed = speed;
    }

    public void updateTemperatureAlert(String timestamp, double temperature, String alert, String severity) {
        this.lastTemperature = temperature;
        updateAlert(timestamp, alert, severity);
    }

    public void updateFuelAlert(String timestamp, double fuelLevel, String alert, String severity) {
        this.lastFuelLevel = fuelLevel;
        updateAlert(timestamp, alert, severity);
    }

    private void updateAlert(String timestamp, String alert, String severity) {
        this.lastAlert = alert;
        this.lastAlertSeverity = severity;
        this.lastAlertTimestamp = timestamp;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public String getLastTimestamp() {
        return lastTimestamp;
    }

    public double getLastLat() {
        return lastLat;
    }

    public double getLastLng() {
        return lastLng;
    }

    public double getLastSpeed() {
        return lastSpeed;
    }

    public Double getLastTemperature() {
        return lastTemperature;
    }

    public Double getLastFuelLevel() {
        return lastFuelLevel;
    }

    public String getLastAlert() {
        return lastAlert;
    }

    public String getLastAlertSeverity() {
        return lastAlertSeverity;
    }

    public String getLastAlertTimestamp() {
        return lastAlertTimestamp;
    }
}
