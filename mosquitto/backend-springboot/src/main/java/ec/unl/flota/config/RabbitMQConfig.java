package ec.unl.flota.config;

import ec.unl.flota.constant.QueueNames;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMQConfig {

    @Bean
    public Queue gpsTelemetryQueue() {
        return QueueBuilder.durable(QueueNames.GPS_TELEMETRY).build();
    }

    @Bean
    public Queue temperatureAlertsQueue() {
        return QueueBuilder.durable(QueueNames.TEMPERATURE_ALERTS).build();
    }

    @Bean
    public Queue fuelAlertsQueue() {
        return QueueBuilder.durable(QueueNames.FUEL_ALERTS).build();
    }

    @Bean
    public Queue notificationsQueue() {
        return QueueBuilder.durable(QueueNames.NOTIFICATIONS).build();
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
