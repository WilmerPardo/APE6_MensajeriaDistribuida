package ec.unl.flota.listener;

import ec.unl.flota.constant.QueueNames;
import ec.unl.flota.dto.GpsTelemetryMessage;
import ec.unl.flota.service.RouteService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RouteConsumer {

    private final RouteService routeService;

    public RouteConsumer(RouteService routeService) {
        this.routeService = routeService;
    }

    @RabbitListener(queues = QueueNames.GPS_TELEMETRY)
    public void consumeGpsTelemetry(GpsTelemetryMessage message) {
        routeService.processGpsTelemetry(message);
    }
}
