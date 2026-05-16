package ec.unl.flota.controller;

import ec.unl.flota.service.MetricsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/monitoring")
public class MonitoringController {

    private final MetricsService metricsService;

    public MonitoringController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "flota-monitoring-service"
        ));
    }

    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Integer>> metrics() {
        return ResponseEntity.ok(Map.of(
                "gpsMessages",       metricsService.getGpsMessages(),
                "temperatureAlerts", metricsService.getTemperatureAlerts(),
                "fuelAlerts",        metricsService.getFuelAlerts(),
                "notifications",     metricsService.getNotifications()
        ));
    }
}
