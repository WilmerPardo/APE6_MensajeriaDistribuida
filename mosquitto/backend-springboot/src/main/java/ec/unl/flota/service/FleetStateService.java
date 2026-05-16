package ec.unl.flota.service;

import ec.unl.flota.dto.FleetStatusResponse;
import ec.unl.flota.dto.FuelAlertMessage;
import ec.unl.flota.dto.GpsTelemetryMessage;
import ec.unl.flota.dto.TemperatureAlertMessage;
import ec.unl.flota.entity.AlertEvent;
import ec.unl.flota.entity.GpsTelemetry;
import ec.unl.flota.entity.VehicleState;
import ec.unl.flota.repository.AlertEventRepository;
import ec.unl.flota.repository.GpsTelemetryRepository;
import ec.unl.flota.repository.VehicleStateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class FleetStateService {

    private static final String TEMP_ALERT_TYPE = "TEMP_ALERT";
    private static final String FUEL_LOW_TYPE = "FUEL_LOW";

    private final GpsTelemetryRepository gpsRepository;
    private final AlertEventRepository alertRepository;
    private final VehicleStateRepository vehicleRepository;

    public FleetStateService(GpsTelemetryRepository gpsRepository,
                             AlertEventRepository alertRepository,
                             VehicleStateRepository vehicleRepository) {
        this.gpsRepository = gpsRepository;
        this.alertRepository = alertRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional
    public void processGpsTelemetry(GpsTelemetryMessage message) {
        gpsRepository.save(new GpsTelemetry(
                message.vehicleId(),
                message.timestamp(),
                message.lat(),
                message.lng(),
                message.speed()
        ));

        VehicleState vehicleState = findOrCreateVehicle(message.vehicleId());
        vehicleState.updateGps(message.timestamp(), message.lat(), message.lng(), message.speed());
        vehicleRepository.save(vehicleState);
    }

    @Transactional
    public void processTemperatureAlert(TemperatureAlertMessage message) {
        String severity = "HIGH";

        alertRepository.save(new AlertEvent(
                message.vehicleId(),
                TEMP_ALERT_TYPE,
                message.message(),
                severity,
                message.timestamp(),
                message.temperature(),
                message.unit(),
                true
        ));

        VehicleState vehicleState = findOrCreateVehicle(message.vehicleId());
        vehicleState.updateTemperatureAlert(
                message.timestamp(),
                message.temperature(),
                "Temperatura alta: " + message.temperature() + " " + message.unit(),
                severity
        );
        vehicleRepository.save(vehicleState);
    }

    @Transactional
    public void processFuelAlert(FuelAlertMessage message) {
        String severity = "MEDIUM";

        alertRepository.save(new AlertEvent(
                message.vehicleId(),
                FUEL_LOW_TYPE,
                message.message(),
                severity,
                message.timestamp(),
                message.fuelLevel(),
                message.unit(),
                true
        ));

        VehicleState vehicleState = findOrCreateVehicle(message.vehicleId());
        vehicleState.updateFuelAlert(
                message.timestamp(),
                message.fuelLevel(),
                "Combustible bajo: " + message.fuelLevel() + " " + message.unit(),
                severity
        );
        vehicleRepository.save(vehicleState);
    }

    @Transactional(readOnly = true)
    public FleetStatusResponse getFleetStatus() {
        return new FleetStatusResponse(
                vehicleRepository.count(),
                gpsRepository.count(),
                alertRepository.countByActiveTrue(),
                alertRepository.countByType(TEMP_ALERT_TYPE),
                alertRepository.countByType(FUEL_LOW_TYPE)
        );
    }

    @Transactional(readOnly = true)
    public List<VehicleState> getVehicles() {
        return vehicleRepository.findAllByOrderByVehicleIdAsc();
    }

    @Transactional(readOnly = true)
    public List<GpsTelemetry> getVehicleTelemetry(String vehicleId) {
        return gpsRepository.findTop50ByVehicleIdOrderByIdDesc(vehicleId);
    }

    @Transactional(readOnly = true)
    public List<AlertEvent> getRecentAlerts() {
        return alertRepository.findTop50ByOrderByIdDesc();
    }

    @Transactional(readOnly = true)
    public Optional<VehicleState> findVehicleState(String vehicleId) {
        return vehicleRepository.findById(vehicleId);
    }

    private VehicleState findOrCreateVehicle(String vehicleId) {
        return vehicleRepository.findById(vehicleId)
                .orElseGet(() -> new VehicleState(vehicleId));
    }
}
