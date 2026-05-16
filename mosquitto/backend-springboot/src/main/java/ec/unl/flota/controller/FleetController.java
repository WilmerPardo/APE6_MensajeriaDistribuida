package ec.unl.flota.controller;

import ec.unl.flota.dto.FleetStatusResponse;
import ec.unl.flota.entity.AlertEvent;
import ec.unl.flota.entity.GpsTelemetry;
import ec.unl.flota.entity.VehicleState;
import ec.unl.flota.service.FleetStateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/fleet")
public class FleetController {

    private final FleetStateService fleetStateService;

    public FleetController(FleetStateService fleetStateService) {
        this.fleetStateService = fleetStateService;
    }

    @GetMapping("/status")
    public ResponseEntity<FleetStatusResponse> getFleetStatus() {
        return ResponseEntity.ok(fleetStateService.getFleetStatus());
    }

    @GetMapping("/vehicles")
    public ResponseEntity<List<VehicleState>> getVehicles() {
        return ResponseEntity.ok(fleetStateService.getVehicles());
    }

    @GetMapping("/vehicle/{id}/telemetria")
    public ResponseEntity<List<GpsTelemetry>> getVehicleTelemetry(@PathVariable String id) {
        return ResponseEntity.ok(fleetStateService.getVehicleTelemetry(id));
    }

    @GetMapping("/vehicle/{id}/status")
    public ResponseEntity<VehicleState> getVehicleStatus(@PathVariable String id) {
        return fleetStateService.findVehicleState(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<AlertEvent>> getAlerts() {
        return ResponseEntity.ok(fleetStateService.getRecentAlerts());
    }
}
