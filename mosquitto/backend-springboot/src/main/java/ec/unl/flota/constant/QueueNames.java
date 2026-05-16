package ec.unl.flota.constant;

public final class QueueNames {

    public static final String GPS_TELEMETRY    = "cola.gps.telemetria";
    public static final String TEMPERATURE_ALERTS = "cola.alertas.temperatura";
    public static final String FUEL_ALERTS      = "cola.combustible.nivel";
    public static final String NOTIFICATIONS    = "cola.notificaciones";

    private QueueNames() {
        // Clase utilitaria: evita instanciación.
    }
}
