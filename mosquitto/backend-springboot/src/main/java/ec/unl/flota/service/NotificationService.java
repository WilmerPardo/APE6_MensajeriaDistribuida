package ec.unl.flota.service;

import ec.unl.flota.dto.NotificationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final MetricsService metricsService;

    public NotificationService(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    /**
     * Simula el envío de una notificación al operador.
     * Para la práctica basta con registrar la notificación en consola.
     */
    public void sendNotification(NotificationMessage message) {
        metricsService.incrementNotifications();

        logger.info(
                "Notificación enviada | vehículo={} | título={} | severidad={} | detalle={}",
                message.vehicleId(),
                message.title(),
                message.severity(),
                message.detail()
        );
    }
}
