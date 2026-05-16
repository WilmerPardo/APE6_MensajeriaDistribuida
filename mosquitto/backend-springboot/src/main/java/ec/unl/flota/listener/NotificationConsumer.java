package ec.unl.flota.listener;

import ec.unl.flota.constant.QueueNames;
import ec.unl.flota.dto.NotificationMessage;
import ec.unl.flota.service.NotificationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

    private final NotificationService notificationService;

    public NotificationConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = QueueNames.NOTIFICATIONS)
    public void consumeNotification(NotificationMessage message) {
        notificationService.sendNotification(message);
    }
}
