package ec.unl.flota.listener;

import ec.unl.flota.constant.QueueNames;
import ec.unl.flota.dto.TemperatureAlertMessage;
import ec.unl.flota.service.AlertService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class TemperatureAlertConsumer {

    private final AlertService alertService;

    public TemperatureAlertConsumer(AlertService alertService) {
        this.alertService = alertService;
    }

    @RabbitListener(queues = QueueNames.TEMPERATURE_ALERTS)
    public void consumeTemperatureAlert(TemperatureAlertMessage message) {
        alertService.processTemperatureAlert(message);
    }
}
