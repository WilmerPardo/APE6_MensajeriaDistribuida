package ec.unl.flota.dto;

public record FleetStatusResponse(
        long totalVehicles,
        long gpsMessages,
        long activeAlerts,
        long temperatureAlerts,
        long fuelAlerts
) {}
