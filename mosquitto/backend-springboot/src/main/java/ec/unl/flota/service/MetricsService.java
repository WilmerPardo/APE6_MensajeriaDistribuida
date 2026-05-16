package ec.unl.flota.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Service
public class MetricsService {

    private final AtomicInteger gpsMessages       = new AtomicInteger();
    private final AtomicInteger temperatureAlerts = new AtomicInteger();
    private final AtomicInteger fuelAlerts        = new AtomicInteger();
    private final AtomicInteger notifications     = new AtomicInteger();

    public void incrementGpsMessages()       { gpsMessages.incrementAndGet(); }
    public void incrementTemperatureAlerts() { temperatureAlerts.incrementAndGet(); }
    public void incrementFuelAlerts()        { fuelAlerts.incrementAndGet(); }
    public void incrementNotifications()     { notifications.incrementAndGet(); }

    public int getGpsMessages()       { return gpsMessages.get(); }
    public int getTemperatureAlerts() { return temperatureAlerts.get(); }
    public int getFuelAlerts()        { return fuelAlerts.get(); }
    public int getNotifications()     { return notifications.get(); }
}
