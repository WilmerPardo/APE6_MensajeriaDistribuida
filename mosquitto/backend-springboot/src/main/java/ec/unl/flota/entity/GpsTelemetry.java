package ec.unl.flota.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class GpsTelemetry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String vehicleId;
    @Column(name = "event_timestamp")
    private String timestamp;
    private double lat;
    private double lng;
    private double speed;

    protected GpsTelemetry() {
        // Constructor requerido por JPA.
    }

    public GpsTelemetry(String vehicleId, String timestamp, double lat, double lng, double speed) {
        this.vehicleId = vehicleId;
        this.timestamp = timestamp;
        this.lat = lat;
        this.lng = lng;
        this.speed = speed;
    }

    public Long getId() {
        return id;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public double getSpeed() {
        return speed;
    }
}
