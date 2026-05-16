# Práctica 5: Mensajería Distribuida con MQTT, RabbitMQ y Spring Boot

## 1. Descripción general

Esta práctica implementa un **Sistema de Monitoreo de Flota Logística** usando mensajería distribuida. La arquitectura combina dos protocolos:

- **MQTT**, usado en la capa IoT para enviar telemetría ligera desde sensores simulados.
- **RabbitMQ / AMQP**, usado en la capa interna para procesar mensajes de forma más confiable mediante colas durables.
- **Spring Boot con Java 17**, usado para implementar los microservicios consumidores de RabbitMQ.

El sistema simula tres vehículos que publican datos de GPS, temperatura y combustible. Los datos viajan por MQTT hacia Mosquitto. Luego, un servicio puente toma los mensajes relevantes y los publica en RabbitMQ para que los microservicios Spring Boot los consuman.

> Nota: Aunque el simulador IoT, el suscriptor local y el puente se implementan en Python porque así se plantea la práctica, la capa de microservicios internos debe implementarse con **Java 17 + Spring Boot**.

---

## 2. Arquitectura propuesta

La arquitectura se organiza en cinco capas principales:

```text
CAPA IOT - SENSORES EMBEBIDOS
    ├── Sensor GPS
    ├── Sensor Temperatura
    ├── Sensor Combustible
    └── Dashboard/API de consulta
            │
            ▼ MQTT puerto 1883

BROKER MQTT - MOSQUITTO
    ├── Topics: flota/{vehiculo_id}/gps
    ├── Topics: flota/{vehiculo_id}/temperatura
    ├── Topics: flota/{vehiculo_id}/combustible
    ├── Suscriptor Python + SQLite
    └── Broker Mosquitto
            │
            ▼ Servicio puente Python

SERVICIO PUENTE MQTT A RABBITMQ
    └── Bridge Service
            ├── Consume mensajes MQTT
            ├── Filtra alertas
            └── Publica mensajes AMQP
            │
            ▼ AMQP puerto 5672

MESSAGE BROKER - RABBITMQ
    ├── cola.gps.telemetria
    ├── cola.alertas.temperatura
    ├── cola.combustible.nivel
    └── cola.notificaciones
            │
            ▼ Consumidores AMQP

MICROSERVICIOS SPRING BOOT
    ├── Servicio de Rutas
    ├── Servicio de Alertas
    ├── Servicio de Notificaciones
    └── API REST / Dashboard
```

---

## 3. Tecnologías requeridas

| Herramienta | Uso |
|---|---|
| Docker | Levantar Mosquitto y RabbitMQ |
| Docker Compose | Orquestar servicios locales |
| Mosquitto | Broker MQTT |
| RabbitMQ | Broker AMQP |
| Python 3 | Simulador IoT, suscriptor SQLite y puente MQTT-RabbitMQ |
| paho-mqtt | Cliente MQTT en Python |
| pika | Cliente RabbitMQ en Python |
| SQLite3 | Almacenamiento local de telemetría |
| Java 17 | Desarrollo de microservicios |
| Spring Boot | Framework para consumidores RabbitMQ y API REST |
| Maven | Gestión de dependencias Java |
| Postman | Pruebas de endpoints REST |

---

## 4. Estructura recomendada del proyecto

```text
flota-mensajeria-distribuida/
│
├── README.md
├── docker-compose.yml
│
├── mosquitto/
│   ├── config/
│   │   └── mosquitto.conf
│   └── data/
│
├── python-iot/
│   ├── requirements.txt
│   ├── sensor_simulator.py
│   ├── mqtt_subscriber_storage.py
│   ├── mqtt_rabbitmq_bridge.py
│   └── telemetria.db              # Se genera automáticamente
│
└── backend-springboot/
    ├── pom.xml
    └── src/
        └── main/
            ├── java/
            │   └── ec/
            │       └── unl/
            │           └── flota/
            │               ├── FleetMonitoringApplication.java
            │               │
            │               ├── config/
            │               │   └── RabbitMQConfig.java
            │               │
            │               ├── constant/
            │               │   └── QueueNames.java
            │               │
            │               ├── controller/
            │               │   └── MonitoringController.java
            │               │
            │               ├── dto/
            │               │   ├── GpsTelemetryMessage.java
            │               │   ├── TemperatureAlertMessage.java
            │               │   ├── FuelAlertMessage.java
            │               │   └── NotificationMessage.java
            │               │
            │               ├── listener/
            │               │   ├── RouteConsumer.java
            │               │   ├── TemperatureAlertConsumer.java
            │               │   ├── FuelLevelConsumer.java
            │               │   └── NotificationConsumer.java
            │               │
            │               ├── service/
            │               │   ├── RouteService.java
            │               │   ├── AlertService.java
            │               │   ├── NotificationService.java
            │               │   └── MetricsService.java
            │               │
            │               └── exception/
            │                   └── MessageProcessingException.java
            │
            └── resources/
                └── application.yml
```

---

## 5. Configuración de Docker

### 5.1. Archivo `docker-compose.yml`

Crear el archivo en la raíz del proyecto:

```yaml
version: '3.8'

services:
  mosquitto:
    image: eclipse-mosquitto:2.0
    container_name: mosquitto_broker
    ports:
      - "1883:1883"
      - "9001:9001"
    volumes:
      - ./mosquitto/config:/mosquitto/config
      - ./mosquitto/data:/mosquitto/data
    restart: unless-stopped

  rabbitmq:
    image: rabbitmq:3-management
    container_name: rabbitmq_broker
    ports:
      - "5672:5672"
      - "15672:15672"
    environment:
      RABBITMQ_DEFAULT_USER: guest
      RABBITMQ_DEFAULT_PASS: guest
    restart: unless-stopped
```

### 5.2. Archivo `mosquitto/config/mosquitto.conf`

```conf
listener 1883 0.0.0.0
allow_anonymous true

listener 9001 0.0.0.0
protocol websockets

persistence true
persistence_location /mosquitto/data
```

### 5.3. Levantar brokers

```bash
docker compose up -d
```

Verificar contenedores:

```bash
docker ps
```

Verificar Mosquitto:

```bash
# Terminal 1
docker exec -it mosquitto_broker mosquitto_sub -h localhost -t "test/topic"

# Terminal 2
docker exec -it mosquitto_broker mosquitto_pub -h localhost -t "test/topic" -m "Hola MQTT"
```

Verificar RabbitMQ:

```text
URL: http://localhost:15672
Usuario: guest
Contraseña: guest
```

---

## 6. Capa IoT con Python

### 6.1. Dependencias Python

Crear `python-iot/requirements.txt`:

```txt
paho-mqtt==1.6.1
pika==1.3.2
```

Instalar dependencias:

```bash
cd python-iot
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
```

---

## 7. Simulador de sensores IoT

Archivo recomendado:

```text
python-iot/sensor_simulator.py
```

Este script debe traducir el pseudocódigo del simulador de sensores y cumplir las siguientes reglas:

- Crear un cliente MQTT.
- Conectarse al broker Mosquitto en `localhost:1883`.
- Simular tres vehículos: `VH-001`, `VH-002`, `VH-003`.
- Generar datos de:
  - GPS.
  - Temperatura.
  - Combustible.
- Publicar mensajes cada 5 segundos.
- Usar topics jerárquicos.

### 7.1. Topics MQTT

```text
flota/VH-001/gps
flota/VH-001/temperatura
flota/VH-001/combustible

flota/VH-002/gps
flota/VH-002/temperatura
flota/VH-002/combustible

flota/VH-003/gps
flota/VH-003/temperatura
flota/VH-003/combustible
```

### 7.2. Formato JSON esperado

GPS:

```json
{
  "vehicle_id": "VH-001",
  "timestamp": "2026-05-08T08:30:00",
  "lat": -2.170900,
  "lng": -79.922400,
  "speed": 55.4
}
```

Temperatura:

```json
{
  "vehicle_id": "VH-001",
  "timestamp": "2026-05-08T08:30:00",
  "temperature": 6.5,
  "unit": "celsius"
}
```

Combustible:

```json
{
  "vehicle_id": "VH-001",
  "timestamp": "2026-05-08T08:30:00",
  "fuel_level": 15.2,
  "unit": "percent"
}
```

### 7.3. Reglas de implementación

El script debe contener funciones separadas:

```text
on_connect()
simular_gps(id_vehiculo)
simular_temperatura(id_vehiculo)
simular_combustible(id_vehiculo)
main()
```

Recomendación de código limpio:

- No escribir toda la lógica dentro de `main()`.
- Usar constantes para broker, puerto y vehículos.
- Comentar únicamente las reglas importantes, por ejemplo, el umbral de temperatura o combustible.
- Convertir los mensajes a JSON antes de publicarlos.
- Manejar correctamente `KeyboardInterrupt` para cerrar la conexión MQTT.

---

## 8. Suscriptor MQTT con SQLite

Archivo recomendado:

```text
python-iot/mqtt_subscriber_storage.py
```

Este componente recibe todos los mensajes publicados por los sensores y los almacena en SQLite.

### 8.1. Topics a suscribir

```text
flota/+/gps
flota/+/temperatura
flota/+/combustible
```

El comodín `+` permite recibir datos de cualquier vehículo, pero solo del tipo de sensor indicado.

### 8.2. Base de datos SQLite

La base de datos debe llamarse:

```text
telemetria.db
```

Debe contener tres tablas separadas.

#### Tabla `gps_data`

```sql
CREATE TABLE IF NOT EXISTS gps_data (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    vehicle_id TEXT NOT NULL,
    lat REAL NOT NULL,
    lng REAL NOT NULL,
    speed REAL NOT NULL,
    timestamp TEXT NOT NULL
);
```

#### Tabla `temp_data`

```sql
CREATE TABLE IF NOT EXISTS temp_data (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    vehicle_id TEXT NOT NULL,
    temperature REAL NOT NULL,
    unit TEXT NOT NULL,
    timestamp TEXT NOT NULL
);
```

#### Tabla `fuel_data`

```sql
CREATE TABLE IF NOT EXISTS fuel_data (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    vehicle_id TEXT NOT NULL,
    fuel_level REAL NOT NULL,
    unit TEXT NOT NULL,
    timestamp TEXT NOT NULL
);
```

### 8.3. Reglas de alerta local

El suscriptor debe imprimir alertas en consola cuando se cumpla alguna condición:

```text
Temperatura > 4 °C
Combustible < 20 %
```

Ejemplo esperado:

```text
ALERTA: VH-002 temperatura alta: 6.8 °C
ALERTA: VH-003 combustible bajo: 14.5 %
```

---

## 9. Servicio puente MQTT a RabbitMQ

Archivo recomendado:

```text
python-iot/mqtt_rabbitmq_bridge.py
```

Este servicio conecta la capa MQTT con RabbitMQ. Debe suscribirse a todos los topics de MQTT, filtrar los mensajes y publicar únicamente los datos que requieren procesamiento en RabbitMQ.

### 9.1. Suscripción MQTT

```text
flota/#
```

El comodín `#` permite recibir todos los topics bajo `flota`.

### 9.2. Colas RabbitMQ requeridas

```text
cola.gps.telemetria
cola.alertas.temperatura
cola.combustible.nivel
cola.notificaciones
```

Todas las colas deben declararse como durables.

### 9.3. Reglas de publicación

| Tipo de mensaje MQTT | Condición | Cola RabbitMQ |
|---|---:|---|
| GPS | Siempre se publica | `cola.gps.telemetria` |
| Temperatura | Solo si `temperature > 4` | `cola.alertas.temperatura` |
| Combustible | Solo si `fuel_level < 20` | `cola.combustible.nivel` |

### 9.4. Mensajes de alerta

Cuando la temperatura exceda el umbral:

```json
{
  "type": "TEMP_ALERT",
  "message": "Temperatura excedida",
  "vehicle_id": "VH-001",
  "timestamp": "2026-05-08T08:30:00",
  "temperature": 6.7,
  "unit": "celsius"
}
```

Cuando el combustible sea bajo:

```json
{
  "type": "FUEL_LOW",
  "message": "Combustible bajo",
  "vehicle_id": "VH-003",
  "timestamp": "2026-05-08T08:30:00",
  "fuel_level": 12.5,
  "unit": "percent"
}
```

### 9.5. Persistencia de mensajes

Al publicar en RabbitMQ, los mensajes deben enviarse como persistentes para evitar pérdida ante reinicios del broker.

En Python con `pika`, usar:

```python
properties=pika.BasicProperties(delivery_mode=2)
```

---

# 10. Microservicios Spring Boot con Java 17

Esta sección corresponde a la implementación principal en **Java y Spring Boot**. Los microservicios consumen mensajes desde RabbitMQ y ejecutan la lógica de negocio.

## 10.1. Crear proyecto Spring Boot

Crear el proyecto con las siguientes dependencias:

- Spring Web
- Spring AMQP
- Validation
- Actuator, opcional

También se puede crear desde terminal:

```bash
mkdir backend-springboot
cd backend-springboot
```

Si se usa Spring Initializr, configurar:

```text
Project: Maven
Language: Java
Spring Boot: 3.x
Java: 17
Group: ec.unl
Artifact: flota
Package name: ec.unl.flota
```

---

## 10.2. Archivo `pom.xml`

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.0</version>
        <relativePath/>
    </parent>

    <groupId>ec.unl</groupId>
    <artifactId>flota</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>flota</name>
    <description>Microservicios Spring Boot para monitoreo de flota con RabbitMQ</description>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-amqp</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

> Si el equipo tiene JDK 21 instalado, el proyecto puede ejecutarse igualmente, pero el `pom.xml` mantiene compatibilidad con Java 17 mediante `<java.version>17</java.version>`.

---

## 10.3. Archivo `application.yml`

Ruta:

```text
backend-springboot/src/main/resources/application.yml
```

Contenido:

```yaml
server:
  port: 8080

spring:
  application:
    name: flota-monitoring-service
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest

management:
  endpoints:
    web:
      exposure:
        include: health,info

app:
  queues:
    gps: cola.gps.telemetria
    temperature-alerts: cola.alertas.temperatura
    fuel-alerts: cola.combustible.nivel
    notifications: cola.notificaciones
```

---

## 10.4. Clase principal

Ruta:

```text
src/main/java/ec/unl/flota/FleetMonitoringApplication.java
```

```java
package ec.unl.flota;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FleetMonitoringApplication {

    public static void main(String[] args) {
        SpringApplication.run(FleetMonitoringApplication.class, args);
    }
}
```

---

## 10.5. Constantes de colas

Ruta:

```text
src/main/java/ec/unl/flota/constant/QueueNames.java
```

```java
package ec.unl.flota.constant;

public final class QueueNames {

    public static final String GPS_TELEMETRY = "cola.gps.telemetria";
    public static final String TEMPERATURE_ALERTS = "cola.alertas.temperatura";
    public static final String FUEL_ALERTS = "cola.combustible.nivel";
    public static final String NOTIFICATIONS = "cola.notificaciones";

    private QueueNames() {
        // Clase utilitaria: evita instanciación.
    }
}
```

---

## 10.6. Configuración RabbitMQ en Java

Ruta:

```text
src/main/java/ec/unl/flota/config/RabbitMQConfig.java
```

```java
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
```

---

## 10.7. DTOs para recibir mensajes

Usar `record` permite escribir clases simples, inmutables y limpias en Java 17.

### `GpsTelemetryMessage.java`

```java
package ec.unl.flota.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GpsTelemetryMessage(
        @JsonProperty("vehicle_id") String vehicleId,
        String timestamp,
        double lat,
        double lng,
        double speed
) {}
```

### `TemperatureAlertMessage.java`

```java
package ec.unl.flota.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TemperatureAlertMessage(
        String type,
        String message,
        @JsonProperty("vehicle_id") String vehicleId,
        String timestamp,
        double temperature,
        String unit
) {}
```

### `FuelAlertMessage.java`

```java
package ec.unl.flota.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FuelAlertMessage(
        String type,
        String message,
        @JsonProperty("vehicle_id") String vehicleId,
        String timestamp,
        @JsonProperty("fuel_level") double fuelLevel,
        String unit
) {}
```

### `NotificationMessage.java`

```java
package ec.unl.flota.dto;

public record NotificationMessage(
        String vehicleId,
        String title,
        String detail,
        String severity,
        String timestamp
) {}
```

---

# 11. Servicios Java

## 11.1. Servicio de métricas

Ruta:

```text
src/main/java/ec/unl/flota/service/MetricsService.java
```

```java
package ec.unl.flota.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Service
public class MetricsService {

    private final AtomicInteger gpsMessages = new AtomicInteger();
    private final AtomicInteger temperatureAlerts = new AtomicInteger();
    private final AtomicInteger fuelAlerts = new AtomicInteger();
    private final AtomicInteger notifications = new AtomicInteger();

    public void incrementGpsMessages() {
        gpsMessages.incrementAndGet();
    }

    public void incrementTemperatureAlerts() {
        temperatureAlerts.incrementAndGet();
    }

    public void incrementFuelAlerts() {
        fuelAlerts.incrementAndGet();
    }

    public void incrementNotifications() {
        notifications.incrementAndGet();
    }

    public int getGpsMessages() {
        return gpsMessages.get();
    }

    public int getTemperatureAlerts() {
        return temperatureAlerts.get();
    }

    public int getFuelAlerts() {
        return fuelAlerts.get();
    }

    public int getNotifications() {
        return notifications.get();
    }
}
```

---

## 11.2. Servicio de rutas

Ruta:

```text
src/main/java/ec/unl/flota/service/RouteService.java
```

```java
package ec.unl.flota.service;

import ec.unl.flota.dto.GpsTelemetryMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RouteService {

    private static final Logger logger = LoggerFactory.getLogger(RouteService.class);

    private final MetricsService metricsService;

    public RouteService(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    /**
     * Procesa la telemetría GPS recibida desde RabbitMQ.
     * En una versión extendida, aquí se podría calcular una ruta óptima.
     */
    public void processGpsTelemetry(GpsTelemetryMessage message) {
        metricsService.incrementGpsMessages();

        logger.info(
                "GPS recibido | vehículo={} | lat={} | lng={} | velocidad={} km/h",
                message.vehicleId(),
                message.lat(),
                message.lng(),
                message.speed()
        );
    }
}
```

---

## 11.3. Servicio de alertas

Ruta:

```text
src/main/java/ec/unl/flota/service/AlertService.java
```

```java
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

    public AlertService(RabbitTemplate rabbitTemplate, MetricsService metricsService) {
        this.rabbitTemplate = rabbitTemplate;
        this.metricsService = metricsService;
    }

    /**
     * Procesa una alerta de temperatura y genera una notificación interna.
     */
    public void processTemperatureAlert(TemperatureAlertMessage message) {
        metricsService.incrementTemperatureAlerts();

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
```

---

## 11.4. Servicio de notificaciones

Ruta:

```text
src/main/java/ec/unl/flota/service/NotificationService.java
```

```java
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
```

---

# 12. Consumidores RabbitMQ en Spring Boot

## 12.1. Consumidor GPS

Ruta:

```text
src/main/java/ec/unl/flota/listener/RouteConsumer.java
```

```java
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
```

## 12.2. Consumidor de alertas de temperatura

Ruta:

```text
src/main/java/ec/unl/flota/listener/TemperatureAlertConsumer.java
```

```java
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
```

## 12.3. Consumidor de alertas de combustible

Ruta:

```text
src/main/java/ec/unl/flota/listener/FuelLevelConsumer.java
```

```java
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
```

## 12.4. Consumidor de notificaciones

Ruta:

```text
src/main/java/ec/unl/flota/listener/NotificationConsumer.java
```

```java
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
```

---

# 13. API REST para consulta de estado

La práctica no requiere un frontend completo. Para validar el funcionamiento del dashboard se puede exponer una API REST simple.

Ruta:

```text
src/main/java/ec/unl/flota/controller/MonitoringController.java
```

```java
package ec.unl.flota.controller;

import ec.unl.flota.service.MetricsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/monitoring")
public class MonitoringController {

    private final MetricsService metricsService;

    public MonitoringController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "flota-monitoring-service"
        ));
    }

    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Integer>> metrics() {
        return ResponseEntity.ok(Map.of(
                "gpsMessages", metricsService.getGpsMessages(),
                "temperatureAlerts", metricsService.getTemperatureAlerts(),
                "fuelAlerts", metricsService.getFuelAlerts(),
                "notifications", metricsService.getNotifications()
        ));
    }
}
```

Probar con:

```bash
curl http://localhost:8080/api/monitoring/health
curl http://localhost:8080/api/monitoring/metrics
```

---

# 14. Traducción de pseudocódigo a lógica Java

Aunque el pseudocódigo de la guía se centra en Python para la capa IoT, su lógica se refleja en Java de la siguiente manera:

| Pseudocódigo original | Traducción en Spring Boot |
|---|---|
| `DECLARAR cola` | `@Bean Queue` en `RabbitMQConfig` |
| `PUBLICAR en RabbitMQ` | `rabbitTemplate.convertAndSend(...)` |
| `SI topic contiene /gps` | El bridge envía a `cola.gps.telemetria`; Java consume con `@RabbitListener` |
| `SI temperature > 4` | El bridge filtra; Java procesa el mensaje como `TemperatureAlertMessage` |
| `SI fuel_level < 20` | El bridge filtra; Java procesa el mensaje como `FuelAlertMessage` |
| `IMPRIMIR alerta` | `logger.warn(...)` en `AlertService` |
| `Enviar notificación` | Crear `NotificationMessage` y publicar en `cola.notificaciones` |
| `Escucha continua` | `@RabbitListener` mantiene el consumidor activo |

La lógica importante debe quedar separada en servicios, no dentro de los consumidores. Los consumidores solo reciben el mensaje y delegan el procesamiento.

---

# 15. Orden recomendado de ejecución

## 15.1. Levantar Mosquitto y RabbitMQ

```bash
docker compose up -d
```

## 15.2. Ejecutar suscriptor con SQLite

```bash
cd python-iot
source .venv/bin/activate
python mqtt_subscriber_storage.py
```

## 15.3. Ejecutar puente MQTT-RabbitMQ

En otra terminal:

```bash
cd python-iot
source .venv/bin/activate
python mqtt_rabbitmq_bridge.py
```

## 15.4. Ejecutar Spring Boot

En otra terminal:

```bash
cd backend-springboot
mvn spring-boot:run
```

## 15.5. Ejecutar simulador de sensores

En otra terminal:

```bash
cd python-iot
source .venv/bin/activate
python sensor_simulator.py
```

---

# 16. Validaciones esperadas

## 16.1. Broker MQTT funcional

Evidencias:

- Contenedor Mosquitto activo.
- Mensaje de prueba recibido en `test/topic`.
- Simulador publicando en `flota/{vehiculo}/{sensor}`.

Comandos:

```bash
docker ps

docker exec -it mosquitto_broker mosquitto_sub -h localhost -t "flota/#"
```

---

## 16.2. Simulador de sensores IoT

Evidencia esperada en consola:

```text
Iniciando simulador de sensores IoT...
[2026-05-08T08:30:00] Datos publicados para VH-001
[2026-05-08T08:30:05] Datos publicados para VH-002
[2026-05-08T08:30:10] Datos publicados para VH-003
```

---

## 16.3. Suscriptor con almacenamiento SQLite

Verificar que se haya creado la base:

```bash
ls python-iot/telemetria.db
```

Consultar datos:

```bash
sqlite3 telemetria.db
```

Dentro de SQLite:

```sql
.tables
SELECT COUNT(*) FROM gps_data;
SELECT COUNT(*) FROM temp_data;
SELECT COUNT(*) FROM fuel_data;
```

---

## 16.4. Servicio puente operativo

Evidencia esperada:

```text
Colas RabbitMQ creadas exitosamente
Bridge MQTT-RabbitMQ iniciado...
Publicado GPS en cola.gps.telemetria
Publicado alerta de temperatura en cola.alertas.temperatura
Publicado alerta de combustible en cola.combustible.nivel
```

Verificar en RabbitMQ Management:

```text
http://localhost:15672
```

Revisar que existan las colas:

```text
cola.gps.telemetria
cola.alertas.temperatura
cola.combustible.nivel
cola.notificaciones
```

---

## 16.5. Microservicios Spring Boot

Evidencia esperada en logs:

```text
GPS recibido | vehículo=VH-001 | lat=-2.17 | lng=-79.92 | velocidad=55.4 km/h
Alerta de temperatura | vehículo=VH-002 | temperatura=6.8 celsius
Alerta de combustible | vehículo=VH-003 | nivel=14.5 percent
Notificación enviada | vehículo=VH-003 | título=Combustible bajo | severidad=MEDIUM
```

Validar API REST:

```bash
curl http://localhost:8080/api/monitoring/health
curl http://localhost:8080/api/monitoring/metrics
```

Respuesta esperada:

```json
{
  "gpsMessages": 12,
  "temperatureAlerts": 3,
  "fuelAlerts": 2,
  "notifications": 5
}
```

---

# 17. Buenas prácticas de código limpio

Aplicar las siguientes recomendaciones durante la implementación:

1. Separar responsabilidades:
   - `listener`: solo recibe mensajes.
   - `service`: contiene reglas de negocio.
   - `dto`: define la estructura de los mensajes.
   - `config`: contiene configuración técnica.
   - `constant`: centraliza nombres de colas.

2. No repetir strings de colas:
   - Usar `QueueNames` en vez de escribir varias veces `cola.gps.telemetria`.

3. Comentar únicamente lo necesario:
   - Comentar reglas de negocio como umbrales de temperatura y combustible.
   - No comentar código obvio como getters, constructores o anotaciones.

4. Usar logs en vez de `System.out.println` en Java:
   - `logger.info(...)` para eventos normales.
   - `logger.warn(...)` para alertas.
   - `logger.error(...)` para errores.

5. Usar DTOs claros:
   - Mantener nombres alineados con el JSON recibido.
   - Usar `@JsonProperty` cuando el JSON use snake_case.

6. Manejar errores:
   - Validar que los mensajes tengan campos requeridos.
   - Registrar errores sin detener toda la aplicación.

7. Mantener la configuración fuera del código:
   - Usar `application.yml` para host, puerto, usuario y contraseña de RabbitMQ.

---

# 18. Errores comunes y solución

## Mosquitto no recibe mensajes

Verificar que el contenedor esté activo:

```bash
docker ps
```

Revisar logs:

```bash
docker logs mosquitto_broker
```

## RabbitMQ no abre en el navegador

Verificar que el puerto `15672` esté expuesto:

```bash
docker ps
```

Abrir:

```text
http://localhost:15672
```

## Spring Boot no consume mensajes

Verificar:

- RabbitMQ está activo.
- Las colas existen.
- El bridge está publicando mensajes.
- `application.yml` usa `localhost`, puerto `5672`, usuario `guest`, contraseña `guest`.

## Error al mapear JSON en Java

Revisar que los DTOs usen `@JsonProperty` cuando el campo venga en snake_case:

```java
@JsonProperty("vehicle_id") String vehicleId
@JsonProperty("fuel_level") double fuelLevel
```

## No aparecen alertas

Recordar que las alertas solo se publican si:

```text
temperature > 4
fuel_level < 20
```

Si se desea probar rápidamente, modificar temporalmente el simulador para generar valores fuera del umbral.

---

# 19. Criterios de entrega

El proyecto debe entregar:

```text
flota-mensajeria-distribuida/
├── README.md
├── docker-compose.yml
├── mosquitto/config/mosquitto.conf
├── python-iot/
│   ├── requirements.txt
│   ├── sensor_simulator.py
│   ├── mqtt_subscriber_storage.py
│   ├── mqtt_rabbitmq_bridge.py
│   └── telemetria.db
└── backend-springboot/
    ├── pom.xml
    └── src/main/...
```

Además, se recomienda adjuntar capturas de:

- Contenedores Docker activos.
- Prueba MQTT con `mosquitto_pub` y `mosquitto_sub`.
- Simulador publicando datos.
- SQLite con datos almacenados.
- RabbitMQ Management mostrando las colas.
- Logs de Spring Boot consumiendo mensajes.
- Respuesta de `/api/monitoring/metrics`.

---

# 20. Resultado final esperado

Al finalizar la práctica se debe comprobar lo siguiente:

- Mosquitto funciona como broker MQTT.
- Los sensores simulados publican telemetría periódicamente.
- El suscriptor Python almacena datos en SQLite.
- El bridge MQTT-RabbitMQ filtra alertas y publica en colas AMQP.
- RabbitMQ contiene colas durables para telemetría, alertas y notificaciones.
- Spring Boot consume las colas con `@RabbitListener`.
- El servicio de alertas genera notificaciones internas.
- La API REST permite consultar el estado básico y métricas del sistema.

Con esto se valida el uso combinado de MQTT para la capa IoT y RabbitMQ con Spring Boot para la capa de procesamiento distribuido.
