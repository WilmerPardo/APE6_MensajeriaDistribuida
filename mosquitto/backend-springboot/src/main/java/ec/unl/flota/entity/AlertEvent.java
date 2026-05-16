package ec.unl.flota.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class AlertEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String vehicleId;
    private String type;
    private String message;
    private String severity;
    @Column(name = "event_timestamp")
    private String timestamp;

    @Column(name = "alert_value")
    private double value;
    private String unit;
    private boolean active;

    protected AlertEvent() {
        // Constructor requerido por JPA.
    }

    public AlertEvent(String vehicleId, String type, String message, String severity,
                      String timestamp, double value, String unit, boolean active) {
        this.vehicleId = vehicleId;
        this.type = type;
        this.message = message;
        this.severity = severity;
        this.timestamp = timestamp;
        this.value = value;
        this.unit = unit;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public String getSeverity() {
        return severity;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public double getValue() {
        return value;
    }

    public String getUnit() {
        return unit;
    }

    public boolean isActive() {
        return active;
    }
}
