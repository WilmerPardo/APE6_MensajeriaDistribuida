package ec.unl.flota.service;

import ec.unl.flota.dto.GpsTelemetryMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RouteService {

    private static final Logger logger = LoggerFactory.getLogger(RouteService.class);

    private final MetricsService metricsService;
    private final FleetStateService fleetStateService;

    public RouteService(MetricsService metricsService, FleetStateService fleetStateService) {
        this.metricsService = metricsService;
        this.fleetStateService = fleetStateService;
    }

    /**
     * Procesa la telemetría GPS recibida desde RabbitMQ.
     * En una versión extendida, aquí se podría calcular una ruta óptima.
     */
    public void processGpsTelemetry(GpsTelemetryMessage message) {
        metricsService.incrementGpsMessages();
        fleetStateService.processGpsTelemetry(message);

        logger.info(
                "GPS recibido | vehículo={} | lat={} | lng={} | velocidad={} km/h",
                message.vehicleId(),
                message.lat(),
                message.lng(),
                message.speed()
        );
    }
}
