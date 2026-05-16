package ec.unl.flota.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GpsTelemetryMessage(
        @JsonProperty("vehicle_id") String vehicleId,
        String timestamp,
        double lat,
        double lng,
        double speed
) {}
