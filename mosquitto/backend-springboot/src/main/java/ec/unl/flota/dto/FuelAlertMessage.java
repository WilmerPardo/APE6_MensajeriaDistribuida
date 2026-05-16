package ec.unl.flota.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FuelAlertMessage(
        String type,
        String message,
        @JsonProperty("vehicle_id") String vehicleId,
        String timestamp,
        @JsonProperty("fuel_level") double fuelLevel,
        String unit
) {}
