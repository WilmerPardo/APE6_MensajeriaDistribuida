package ec.unl.flota.service;

import ec.unl.flota.constant.QueueNames;
import ec.unl.flota.dto.FuelAlertMessage;
import ec.unl.flota.dto.NotificationMessage;
import ec.unl.flota.dto.TemperatureAlertMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class AlertService {

    private static final Logger logger = LoggerFactory.getLogger(AlertService.class);

    private final RabbitTemplate rabbitTemplate;
    private final MetricsService metricsService;
    private final FleetStateService fleetStateService;

    public AlertService(RabbitTemplate rabbitTemplate,
                        MetricsService metricsService,
                        FleetStateService fleetStateService) {
        this.rabbitTemplate  = rabbitTemplate;
        this.metricsService  = metricsService;
        this.fleetStateService = fleetStateService;
    }

    /**
     * Procesa una alerta de temperatura y genera una notificación interna.
     */
    public void processTemperatureAlert(TemperatureAlertMessage message) {
        metricsService.incrementTemperatureAlerts();
        fleetStateService.processTemperatureAlert(message);

        logger.warn(
                "Alerta de temperatura | vehículo={} | temperatura={} {}",
                message.vehicleId(),
                message.temperature(),
                message.unit()
        );

        NotificationMessage notification = new NotificationMessage(
                message.vehicleId(),
                "Alerta de temperatura",
                "La temperatura excedió el umbral permitido: " + message.temperature() + " " + message.unit(),
                "HIGH",
                message.timestamp()
        );

        rabbitTemplate.convertAndSend(QueueNames.NOTIFICATIONS, notification);
    }

    /**
     * Procesa una alerta de combustible y genera una notificación interna.
     */
    public void processFuelAlert(FuelAlertMessage message) {
        metricsService.incrementFuelAlerts();
        fleetStateService.processFuelAlert(message);

        logger.warn(
                "Alerta de combustible | vehículo={} | nivel={} {}",
                message.vehicleId(),
                message.fuelLevel(),
                message.unit()
        );

        NotificationMessage notification = new NotificationMessage(
                message.vehicleId(),
                "Combustible bajo",
                "El nivel de combustible está por debajo del 20%: " + message.fuelLevel() + "%",
                "MEDIUM",
                message.timestamp()
        );

        rabbitTemplate.convertAndSend(QueueNames.NOTIFICATIONS, notification);
    }
}
