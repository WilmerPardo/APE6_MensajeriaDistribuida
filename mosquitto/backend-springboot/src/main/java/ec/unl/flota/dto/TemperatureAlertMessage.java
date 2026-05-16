package ec.unl.flota.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TemperatureAlertMessage(
        String type,
        String message,
        @JsonProperty("vehicle_id") String vehicleId,
        String timestamp,
        double temperature,
        String unit
) {}
