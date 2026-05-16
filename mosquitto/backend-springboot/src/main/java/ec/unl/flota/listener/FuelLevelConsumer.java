package ec.unl.flota.listener;

import ec.unl.flota.constant.QueueNames;
import ec.unl.flota.dto.FuelAlertMessage;
import ec.unl.flota.service.AlertService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class FuelLevelConsumer {

    private final AlertService alertService;

    public FuelLevelConsumer(AlertService alertService) {
        this.alertService = alertService;
    }

    @RabbitListener(queues = QueueNames.FUEL_ALERTS)
    public void consumeFuelAlert(FuelAlertMessage message) {
        alertService.processFuelAlert(message);
    }
}
