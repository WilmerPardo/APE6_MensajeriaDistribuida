package ec.unl.flota.dto;

public record NotificationMessage(
        String vehicleId,
        String title,
        String detail,
        String severity,
        String timestamp
) {}
