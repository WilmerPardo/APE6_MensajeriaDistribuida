# Parte 2: Microservicios con RabbitMQ y Spring Boot

## Sistema de monitoreo de flota logística

Este README sirve como guía para continuar el proyecto ya realizado en la **Parte 1**, donde se implementó la capa IoT con MQTT, Mosquitto, simulador de sensores, suscriptor con SQLite y servicio puente MQTT → RabbitMQ.

En esta **Parte 2** se debe implementar la capa de procesamiento con **Spring Boot + RabbitMQ + Java 21**, respetando el flujo de trabajo propuesto en la guía y traduciendo los pseudocódigos a código Java limpio, organizado y mantenible.

---

## 1. Objetivo de la Parte 2

Implementar una aplicación Spring Boot que consuma mensajes desde RabbitMQ, procese telemetría GPS y alertas, almacene el estado de la flota y exponga una API REST para consultar la información procesada.

Al finalizar, el sistema debe demostrar una integración end-to-end:

```text
Simulador de sensores IoT
        ↓
Broker MQTT Mosquitto
        ↓
Servicio puente MQTT → RabbitMQ
        ↓
RabbitMQ / AMQP
        ↓
Microservicios Spring Boot
        ↓
API REST de consulta de flota
```

---

## 2. Resultados esperados

### 2.1. Microservicios Spring Boot

Una aplicación Spring Boot con Java 21 que incluya:

- Consumidor RabbitMQ para datos GPS.
- Consumidores RabbitMQ para alertas de temperatura y combustible.
- Procesamiento de alertas y generación de notificaciones.
- Almacenamiento en H2 usando Spring Data JPA.
- API REST para consultar:
  - Estado general de la flota.
  - Telemetría GPS por vehículo.
  - Alertas generadas.
  - Estado individual de un vehículo.

### 2.2. Integración end-to-end

El flujo completo debe funcionar de la siguiente manera:

1. El simulador Python publica datos en MQTT.
2. Mosquitto recibe los topics `flota/{vehiculo}/{sensor}`.
3. El bridge MQTT → RabbitMQ consume MQTT y publica en RabbitMQ.
4. RabbitMQ entrega los mensajes a las colas configuradas.
5. Spring Boot consume las colas con `@RabbitListener`.
6. Los servicios Java procesan GPS, alertas y notificaciones.
7. La API REST permite consultar el estado actualizado de la flota.

---

## 3. Tecnologías necesarias

| Tecnología | Uso |
|---|---|
| Java 21 | Lenguaje principal para la Parte 2 |
| Spring Boot 3.x | Framework backend |
| Spring Web | API REST |
| Spring AMQP | Integración con RabbitMQ |
| Spring Data JPA | Persistencia de datos |
| H2 Database | Base de datos en memoria para la práctica |
| RabbitMQ | Broker AMQP |
| Docker | Ejecución local de RabbitMQ |
| Maven | Gestión del proyecto Java |
| Postman o curl | Pruebas de la API REST |

---

## 4. Estructura recomendada del proyecto

La Parte 2 puede agregarse dentro del repositorio existente en una carpeta llamada `backend-springboot`.

```text
flota-mensajeria-distribuida/
│
├── README.md
├── docker-compose-rabbitmq.yml
│
├── python-iot/
│   ├── sensor_simulator.py
│   ├── mqtt_subscriber_storage.py
│   ├── mqtt_rabbitmq_bridge.py
│   └── telemetria.db
│
└── backend-springboot/
    └── fleet-monitor/
        ├── pom.xml
        └── src/
            └── main/
                ├── java/
                │   └── com/
                │       └── fleet/
                │           └── monitor/
                │               ├── FleetMonitorApplication.java
                │               │
                │               ├── config/
                │               │   └── RabbitMQConfig.java
                │               │
                │               ├── controller/
                │               │   └── FleetController.java
                │               │
                │               ├── consumer/
                │               │   ├── GpsConsumer.java
                │               │   ├── AlertConsumer.java
                │               │   └── NotificationConsumer.java
                │               │
                │               ├── dto/
                │               │   ├── GpsMessage.java
                │               │   ├── TemperatureAlertMessage.java
                │               │   ├── FuelAlertMessage.java
                │               │   ├── NotificationMessage.java
                │               │   └── FleetStatusResponse.java
                │               │
                │               ├── entity/
                │               │   ├── GpsTelemetry.java
                │               │   ├── AlertEvent.java
                │               │   └── VehicleState.java
                │               │
                │               ├── repository/
                │               │   ├── GpsTelemetryRepository.java
                │               │   ├── AlertEventRepository.java
                │               │   └── VehicleStateRepository.java
                │               │
                │               └── service/
                │                   ├── FleetStateService.java
                │                   └── NotificationService.java
                │
                └── resources/
                    └── application.yml
```

---

## 5. Configuración del entorno RabbitMQ

Crear el archivo `docker-compose-rabbitmq.yml` en la raíz del proyecto.

```yaml
version: '3.8'

services:
  rabbitmq:
    image: rabbitmq:3-management
    container_name: rabbitmq_broker
    ports:
      - "5672:5672"
      - "15672:15672"
    environment:
      RABBITMQ_DEFAULT_USER: admin
      RABBITMQ_DEFAULT_PASS: admin123
    restart: unless-stopped
```

Levantar RabbitMQ:

```bash
docker-compose -f docker-compose-rabbitmq.yml up -d
```

Verificar que el contenedor esté activo:

```bash
docker ps
```

Acceder a RabbitMQ Management:

```text
URL: http://localhost:15672
Usuario: admin
Contraseña: admin123
```

---

## 6. Actividad 5: Crear proyecto Spring Boot

Crear el proyecto usando Spring Initializr o desde el IDE.

Configuración recomendada:

```text
Project: Maven
Language: Java
Spring Boot: 3.x
Group: com.fleet.monitor
Artifact: fleet-monitor
Name: fleet-monitor
Package name: com.fleet.monitor
Java: 21
Packaging: Jar
```

Dependencias necesarias:

```text
Spring Web
Spring AMQP
Spring Data JPA
H2 Database
Validation
Spring Boot Actuator, opcional
```

También se puede crear manualmente la carpeta:

```bash
mkdir -p backend-springboot
cd backend-springboot
```

---

## 7. Archivo `pom.xml`

Ruta:

```text
backend-springboot/fleet-monitor/pom.xml
```

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.5</version>
        <relativePath/>
    </parent>

    <groupId>com.fleet.monitor</groupId>
    <artifactId>fleet-monitor</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>fleet-monitor</name>
    <description>Microservicios Spring Boot con RabbitMQ para monitoreo de flota logística</description>

    <properties>
        <java.version>21</java.version>
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
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
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

Verificar Java 21:

```bash
java -version
javac -version
```

Ejecutar el proyecto:

```bash
mvn spring-boot:run
```

---

## 8. Configuración `application.yml`

Ruta:

```text
src/main/resources/application.yml
```

```yaml
server:
  port: 8080

spring:
  application:
    name: fleet-monitor

  rabbitmq:
    host: localhost
    port: 5672
    username: admin
    password: admin123

  datasource:
    url: jdbc:h2:mem:fleetdb
    driver-class-name: org.h2.Driver
    username: sa
    password:

  h2:
    console:
      enabled: true
      path: /h2-console

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        format_sql: true

management:
  endpoints:
    web:
      exposure:
        include: health,info
```

Acceso a la consola H2:

```text
URL: http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:fleetdb
User: sa
Password: vacío
```

---

## 9. Clase principal

Ruta:

```text
src/main/java/com/fleet/monitor/FleetMonitorApplication.java
```

```java
package com.fleet.monitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FleetMonitorApplication {

    public static void main(String[] args) {
        SpringApplication.run(FleetMonitorApplication.class, args);
    }
}
```

---

## 10. Traducción del pseudocódigo a Spring Boot

La guía propone declarar colas, consumir mensajes y procesar GPS/alertas. En Spring Boot, esa lógica se traduce así:

| Pseudocódigo / proceso | Traducción en Java 21 + Spring Boot |
|---|---|
| Declarar exchange RabbitMQ | `@Bean DirectExchange` en `RabbitMQConfig` |
| Declarar colas durables | `QueueBuilder.durable(...)` |
| Enlazar cola con routing key | `BindingBuilder.bind(...).to(...).with(...)` |
| Consumir cola GPS | `@RabbitListener(queues = RabbitMQConfig.GPS_QUEUE)` |
| Procesar mensaje GPS | `GpsConsumer` delega a `FleetStateService` |
| Guardar telemetría | `GpsTelemetryRepository.save(...)` |
| Consumir alerta de temperatura | `AlertConsumer.consumeTempAlert(...)` |
| Consumir alerta de combustible | `AlertConsumer.consumeFuelAlert(...)` |
| Reenviar alerta a notificaciones | `NotificationService.publishNotification(...)` |
| Consultar estado de flota | `FleetController` con endpoints REST |

Regla de buena práctica: los consumidores solo deben recibir el mensaje y delegar la lógica. La lógica de negocio debe estar en servicios.

---

## 11. Configuración RabbitMQ en Java

Ruta:

```text
src/main/java/com/fleet/monitor/config/RabbitMQConfig.java
```

```java
package com.fleet.monitor.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMQConfig {

    public static final String GPS_QUEUE = "cola.gps.telemetria";
    public static final String TEMP_ALERT_QUEUE = "cola.alertas.temperatura";
    public static final String FUEL_QUEUE = "cola.combustible.nivel";
    public static final String NOTIFICATION_QUEUE = "cola.notificaciones";

    public static final String FLEET_EXCHANGE = "exchange.fleet";

    public static final String GPS_ROUTING_KEY = "gps.routing";
    public static final String TEMP_ROUTING_KEY = "temp.alert";
    public static final String FUEL_ROUTING_KEY = "fuel.routing";
    public static final String NOTIFICATION_ROUTING_KEY = "notification.routing";

    @Bean
    public DirectExchange fleetExchange() {
        return new DirectExchange(FLEET_EXCHANGE, true, false);
    }

    @Bean
    public Queue gpsQueue() {
        return QueueBuilder.durable(GPS_QUEUE).build();
    }

    @Bean
    public Queue tempAlertQueue() {
        return QueueBuilder.durable(TEMP_ALERT_QUEUE).build();
    }

    @Bean
    public Queue fuelQueue() {
        return QueueBuilder.durable(FUEL_QUEUE).build();
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE).build();
    }

    @Bean
    public Binding gpsBinding(Queue gpsQueue, DirectExchange fleetExchange) {
        return BindingBuilder.bind(gpsQueue).to(fleetExchange).with(GPS_ROUTING_KEY);
    }

    @Bean
    public Binding tempBinding(Queue tempAlertQueue, DirectExchange fleetExchange) {
        return BindingBuilder.bind(tempAlertQueue).to(fleetExchange).with(TEMP_ROUTING_KEY);
    }

    @Bean
    public Binding fuelBinding(Queue fuelQueue, DirectExchange fleetExchange) {
        return BindingBuilder.bind(fuelQueue).to(fleetExchange).with(FUEL_ROUTING_KEY);
    }

    @Bean
    public Binding notificationBinding(Queue notificationQueue, DirectExchange fleetExchange) {
        return BindingBuilder.bind(notificationQueue).to(fleetExchange).with(NOTIFICATION_ROUTING_KEY);
    }
}
```

> Nota: Si el bridge Python de la Parte 1 publica directamente a las colas, Spring Boot igual puede consumirlas. Si se desea usar exchange y routing keys, actualizar el bridge para publicar en `exchange.fleet` con las routing keys anteriores.

---

## 12. DTOs de mensajes

Los DTOs representan el JSON recibido desde RabbitMQ. Se usan `record` porque Java 21 permite estructuras limpias, inmutables y fáciles de leer.

### 12.1. `GpsMessage.java`

Ruta:

```text
src/main/java/com/fleet/monitor/dto/GpsMessage.java
```

```java
package com.fleet.monitor.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GpsMessage(
        @JsonProperty("vehicle_id") String vehicleId,
        String timestamp,
        double lat,
        double lng,
        double speed
) {}
```

### 12.2. `TemperatureAlertMessage.java`

```java
package com.fleet.monitor.dto;

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

### 12.3. `FuelAlertMessage.java`

```java
package com.fleet.monitor.dto;

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

### 12.4. `NotificationMessage.java`

```java
package com.fleet.monitor.dto;

public record NotificationMessage(
        String vehicleId,
        String title,
        String detail,
        String severity,
        String timestamp
) {}
```

### 12.5. `FleetStatusResponse.java`

```java
package com.fleet.monitor.dto;

public record FleetStatusResponse(
        long totalVehicles,
        long gpsMessages,
        long activeAlerts,
        long temperatureAlerts,
        long fuelAlerts
) {}
```

---

## 13. Entidades JPA

### 13.1. `GpsTelemetry.java`

Ruta:

```text
src/main/java/com/fleet/monitor/entity/GpsTelemetry.java
```

```java
package com.fleet.monitor.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class GpsTelemetry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String vehicleId;
    private String timestamp;
    private double lat;
    private double lng;
    private double speed;

    protected GpsTelemetry() {
        // Constructor requerido por JPA.
    }

    public GpsTelemetry(String vehicleId, String timestamp, double lat, double lng, double speed) {
        this.vehicleId = vehicleId;
        this.timestamp = timestamp;
        this.lat = lat;
        this.lng = lng;
        this.speed = speed;
    }

    public Long getId() { return id; }
    public String getVehicleId() { return vehicleId; }
    public String getTimestamp() { return timestamp; }
    public double getLat() { return lat; }
    public double getLng() { return lng; }
    public double getSpeed() { return speed; }
}
```

### 13.2. `AlertEvent.java`

```java
package com.fleet.monitor.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class AlertEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String vehicleId;
    private String type;
    private String message;
    private String severity;
    private String timestamp;
    private double value;
    private boolean active;

    protected AlertEvent() {
        // Constructor requerido por JPA.
    }

    public AlertEvent(String vehicleId, String type, String message, String severity,
                      String timestamp, double value, boolean active) {
        this.vehicleId = vehicleId;
        this.type = type;
        this.message = message;
        this.severity = severity;
        this.timestamp = timestamp;
        this.value = value;
        this.active = active;
    }

    public Long getId() { return id; }
    public String getVehicleId() { return vehicleId; }
    public String getType() { return type; }
    public String getMessage() { return message; }
    public String getSeverity() { return severity; }
    public String getTimestamp() { return timestamp; }
    public double getValue() { return value; }
    public boolean isActive() { return active; }
}
```

### 13.3. `VehicleState.java`

```java
package com.fleet.monitor.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class VehicleState {

    @Id
    private String vehicleId;

    private String lastTimestamp;
    private double lastLat;
    private double lastLng;
    private double lastSpeed;
    private String lastAlert;

    protected VehicleState() {
        // Constructor requerido por JPA.
    }

    public VehicleState(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public void updateGps(String timestamp, double lat, double lng, double speed) {
        this.lastTimestamp = timestamp;
        this.lastLat = lat;
        this.lastLng = lng;
        this.lastSpeed = speed;
    }

    public void updateAlert(String lastAlert) {
        this.lastAlert = lastAlert;
    }

    public String getVehicleId() { return vehicleId; }
    public String getLastTimestamp() { return lastTimestamp; }
    public double getLastLat() { return lastLat; }
    public double getLastLng() { return lastLng; }
    public double getLastSpeed() { return lastSpeed; }
    public String getLastAlert() { return lastAlert; }
}
```

---

## 14. Repositorios

### 14.1. `GpsTelemetryRepository.java`

```java
package com.fleet.monitor.repository;

import com.fleet.monitor.entity.GpsTelemetry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GpsTelemetryRepository extends JpaRepository<GpsTelemetry, Long> {
    List<GpsTelemetry> findTop50ByVehicleIdOrderByIdDesc(String vehicleId);
}
```

### 14.2. `AlertEventRepository.java`

```java
package com.fleet.monitor.repository;

import com.fleet.monitor.entity.AlertEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertEventRepository extends JpaRepository<AlertEvent, Long> {
    long countByActiveTrue();
    long countByType(String type);
    List<AlertEvent> findTop50ByOrderByIdDesc();
}
```

### 14.3. `VehicleStateRepository.java`

```java
package com.fleet.monitor.repository;

import com.fleet.monitor.entity.VehicleState;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleStateRepository extends JpaRepository<VehicleState, String> {
}
```

---

## 15. Servicios de negocio

### 15.1. `FleetStateService.java`

Ruta:

```text
src/main/java/com/fleet/monitor/service/FleetStateService.java
```

```java
package com.fleet.monitor.service;

import com.fleet.monitor.dto.FleetStatusResponse;
import com.fleet.monitor.dto.FuelAlertMessage;
import com.fleet.monitor.dto.GpsMessage;
import com.fleet.monitor.dto.TemperatureAlertMessage;
import com.fleet.monitor.entity.AlertEvent;
import com.fleet.monitor.entity.GpsTelemetry;
import com.fleet.monitor.entity.VehicleState;
import com.fleet.monitor.repository.AlertEventRepository;
import com.fleet.monitor.repository.GpsTelemetryRepository;
import com.fleet.monitor.repository.VehicleStateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FleetStateService {

    private static final String TEMP_ALERT_TYPE = "TEMP_ALERT";
    private static final String FUEL_LOW_TYPE = "FUEL_LOW";

    private final GpsTelemetryRepository gpsRepository;
    private final AlertEventRepository alertRepository;
    private final VehicleStateRepository vehicleRepository;

    public FleetStateService(GpsTelemetryRepository gpsRepository,
                             AlertEventRepository alertRepository,
                             VehicleStateRepository vehicleRepository) {
        this.gpsRepository = gpsRepository;
        this.alertRepository = alertRepository;
        this.vehicleRepository = vehicleRepository;
    }

    /**
     * Guarda la telemetría GPS y actualiza el último estado conocido del vehículo.
     */
    @Transactional
    public void processGps(GpsMessage message) {
        gpsRepository.save(new GpsTelemetry(
                message.vehicleId(),
                message.timestamp(),
                message.lat(),
                message.lng(),
                message.speed()
        ));

        VehicleState vehicleState = vehicleRepository
                .findById(message.vehicleId())
                .orElseGet(() -> new VehicleState(message.vehicleId()));

        vehicleState.updateGps(message.timestamp(), message.lat(), message.lng(), message.speed());
        vehicleRepository.save(vehicleState);
    }

    /**
     * Registra una alerta de temperatura generada por el bridge MQTT-RabbitMQ.
     */
    @Transactional
    public void processTemperatureAlert(TemperatureAlertMessage message) {
        alertRepository.save(new AlertEvent(
                message.vehicleId(),
                TEMP_ALERT_TYPE,
                message.message(),
                "HIGH",
                message.timestamp(),
                message.temperature(),
                true
        ));

        updateVehicleAlert(message.vehicleId(), "Temperatura alta: " + message.temperature() + " " + message.unit());
    }

    /**
     * Registra una alerta de combustible bajo generada por el bridge MQTT-RabbitMQ.
     */
    @Transactional
    public void processFuelAlert(FuelAlertMessage message) {
        alertRepository.save(new AlertEvent(
                message.vehicleId(),
                FUEL_LOW_TYPE,
                message.message(),
                "MEDIUM",
                message.timestamp(),
                message.fuelLevel(),
                true
        ));

        updateVehicleAlert(message.vehicleId(), "Combustible bajo: " + message.fuelLevel() + " " + message.unit());
    }

    private void updateVehicleAlert(String vehicleId, String alertText) {
        VehicleState vehicleState = vehicleRepository
                .findById(vehicleId)
                .orElseGet(() -> new VehicleState(vehicleId));

        vehicleState.updateAlert(alertText);
        vehicleRepository.save(vehicleState);
    }

    public FleetStatusResponse getFleetStatus() {
        return new FleetStatusResponse(
                vehicleRepository.count(),
                gpsRepository.count(),
                alertRepository.countByActiveTrue(),
                alertRepository.countByType(TEMP_ALERT_TYPE),
                alertRepository.countByType(FUEL_LOW_TYPE)
        );
    }

    public List<GpsTelemetry> getVehicleTelemetry(String vehicleId) {
        return gpsRepository.findTop50ByVehicleIdOrderByIdDesc(vehicleId);
    }

    public List<AlertEvent> getRecentAlerts() {
        return alertRepository.findTop50ByOrderByIdDesc();
    }

    public VehicleState getVehicleState(String vehicleId) {
        return vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("No existe estado para el vehículo: " + vehicleId));
    }
}
```

### 15.2. `NotificationService.java`

```java
package com.fleet.monitor.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleet.monitor.config.RabbitMQConfig;
import com.fleet.monitor.dto.NotificationMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public NotificationService(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Publica una notificación interna en RabbitMQ para que otro consumidor pueda procesarla.
     */
    public void publishNotification(NotificationMessage notification) {
        try {
            String json = objectMapper.writeValueAsString(notification);
            rabbitTemplate.convertAndSend(RabbitMQConfig.NOTIFICATION_QUEUE, json);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo serializar la notificación", e);
        }
    }
}
```

---

## 16. Actividad 6: Servicio de telemetría GPS

El consumidor GPS debe escuchar la cola `cola.gps.telemetria`, transformar el JSON recibido a `GpsMessage` y guardar la información usando `FleetStateService`.

Ruta:

```text
src/main/java/com/fleet/monitor/consumer/GpsConsumer.java
```

```java
package com.fleet.monitor.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleet.monitor.config.RabbitMQConfig;
import com.fleet.monitor.dto.GpsMessage;
import com.fleet.monitor.service.FleetStateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class GpsConsumer {

    private static final Logger log = LoggerFactory.getLogger(GpsConsumer.class);

    private final ObjectMapper objectMapper;
    private final FleetStateService fleetStateService;

    public GpsConsumer(ObjectMapper objectMapper, FleetStateService fleetStateService) {
        this.objectMapper = objectMapper;
        this.fleetStateService = fleetStateService;
    }

    @RabbitListener(queues = RabbitMQConfig.GPS_QUEUE)
    public void consumeGps(String message) {
        try {
            GpsMessage gpsMessage = objectMapper.readValue(message, GpsMessage.class);
            fleetStateService.processGps(gpsMessage);

            log.info("GPS recibido | vehículo={} | lat={} | lng={} | velocidad={} km/h",
                    gpsMessage.vehicleId(), gpsMessage.lat(), gpsMessage.lng(), gpsMessage.speed());
        } catch (Exception e) {
            log.error("Error procesando GPS. Mensaje recibido: {}", message, e);
        }
    }
}
```

---

## 17. Actividad 7: Servicio de alertas

El servicio de alertas debe consumir mensajes desde:

```text
cola.alertas.temperatura
cola.combustible.nivel
```

Después de procesarlos, debe publicar una notificación en:

```text
cola.notificaciones
```

Ruta:

```text
src/main/java/com/fleet/monitor/consumer/AlertConsumer.java
```

```java
package com.fleet.monitor.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleet.monitor.config.RabbitMQConfig;
import com.fleet.monitor.dto.FuelAlertMessage;
import com.fleet.monitor.dto.NotificationMessage;
import com.fleet.monitor.dto.TemperatureAlertMessage;
import com.fleet.monitor.service.FleetStateService;
import com.fleet.monitor.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class AlertConsumer {

    private static final Logger log = LoggerFactory.getLogger(AlertConsumer.class);

    private final ObjectMapper objectMapper;
    private final FleetStateService fleetStateService;
    private final NotificationService notificationService;

    public AlertConsumer(ObjectMapper objectMapper,
                         FleetStateService fleetStateService,
                         NotificationService notificationService) {
        this.objectMapper = objectMapper;
        this.fleetStateService = fleetStateService;
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = RabbitMQConfig.TEMP_ALERT_QUEUE)
    public void consumeTempAlert(String message) {
        try {
            TemperatureAlertMessage alert = objectMapper.readValue(message, TemperatureAlertMessage.class);
            fleetStateService.processTemperatureAlert(alert);

            log.warn("ALERTA TEMPERATURA | vehículo={} | temperatura={} {}",
                    alert.vehicleId(), alert.temperature(), alert.unit());

            notificationService.publishNotification(new NotificationMessage(
                    alert.vehicleId(),
                    "Alerta de temperatura",
                    "Temperatura excedida: " + alert.temperature() + " " + alert.unit(),
                    "HIGH",
                    alert.timestamp()
            ));
        } catch (Exception e) {
            log.error("Error procesando alerta de temperatura. Mensaje recibido: {}", message, e);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.FUEL_QUEUE)
    public void consumeFuelAlert(String message) {
        try {
            FuelAlertMessage alert = objectMapper.readValue(message, FuelAlertMessage.class);
            fleetStateService.processFuelAlert(alert);

            log.warn("ALERTA COMBUSTIBLE | vehículo={} | nivel={} {}",
                    alert.vehicleId(), alert.fuelLevel(), alert.unit());

            notificationService.publishNotification(new NotificationMessage(
                    alert.vehicleId(),
                    "Combustible bajo",
                    "Nivel de combustible bajo: " + alert.fuelLevel() + " " + alert.unit(),
                    "MEDIUM",
                    alert.timestamp()
            ));
        } catch (Exception e) {
            log.error("Error procesando alerta de combustible. Mensaje recibido: {}", message, e);
        }
    }
}
```

---

## 18. Consumidor de notificaciones

Este consumidor demuestra que las alertas se transforman y se reenvían a una cola centralizada de notificaciones.

Ruta:

```text
src/main/java/com/fleet/monitor/consumer/NotificationConsumer.java
```

```java
package com.fleet.monitor.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleet.monitor.config.RabbitMQConfig;
import com.fleet.monitor.dto.NotificationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    private final ObjectMapper objectMapper;

    public NotificationConsumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void consumeNotification(String message) {
        try {
            NotificationMessage notification = objectMapper.readValue(message, NotificationMessage.class);

            log.info("NOTIFICACIÓN | vehículo={} | título={} | severidad={} | detalle={}",
                    notification.vehicleId(),
                    notification.title(),
                    notification.severity(),
                    notification.detail());
        } catch (Exception e) {
            log.error("Error procesando notificación. Mensaje recibido: {}", message, e);
        }
    }
}
```

---

## 19. Actividad 8: API REST de consulta

La API REST debe permitir consultar el estado procesado por los consumidores RabbitMQ.

Ruta:

```text
src/main/java/com/fleet/monitor/controller/FleetController.java
```

```java
package com.fleet.monitor.controller;

import com.fleet.monitor.dto.FleetStatusResponse;
import com.fleet.monitor.entity.AlertEvent;
import com.fleet.monitor.entity.GpsTelemetry;
import com.fleet.monitor.entity.VehicleState;
import com.fleet.monitor.service.FleetStateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/fleet")
public class FleetController {

    private final FleetStateService fleetStateService;

    public FleetController(FleetStateService fleetStateService) {
        this.fleetStateService = fleetStateService;
    }

    @GetMapping("/status")
    public ResponseEntity<FleetStatusResponse> getFleetStatus() {
        return ResponseEntity.ok(fleetStateService.getFleetStatus());
    }

    @GetMapping("/vehicle/{id}/telemetria")
    public ResponseEntity<List<GpsTelemetry>> getTelemetria(@PathVariable String id) {
        return ResponseEntity.ok(fleetStateService.getVehicleTelemetry(id));
    }

    @GetMapping("/vehicle/{id}/status")
    public ResponseEntity<VehicleState> getVehicleStatus(@PathVariable String id) {
        return ResponseEntity.ok(fleetStateService.getVehicleState(id));
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<AlertEvent>> getAlerts() {
        return ResponseEntity.ok(fleetStateService.getRecentAlerts());
    }
}
```

Comandos de prueba:

```bash
curl http://localhost:8080/api/fleet/status
curl http://localhost:8080/api/fleet/vehicle/VH-001/telemetria
curl http://localhost:8080/api/fleet/vehicle/VH-001/status
curl http://localhost:8080/api/fleet/alerts
```

Respuesta esperada para `/api/fleet/status`:

```json
{
  "totalVehicles": 3,
  "gpsMessages": 30,
  "activeAlerts": 5,
  "temperatureAlerts": 3,
  "fuelAlerts": 2
}
```

---

## 20. Orden de ejecución del sistema completo

Para validar la integración end-to-end, ejecutar los componentes en este orden.

### 20.1. Levantar RabbitMQ

```bash
docker-compose -f docker-compose-rabbitmq.yml up -d
```

Verificar en:

```text
http://localhost:15672
```

### 20.2. Ejecutar el servicio puente MQTT → RabbitMQ

Desde la carpeta de la Parte 1:

```bash
cd python-iot
source .venv/bin/activate
python mqtt_rabbitmq_bridge.py
```

El bridge debe publicar en estas colas:

```text
cola.gps.telemetria
cola.alertas.temperatura
cola.combustible.nivel
cola.notificaciones
```

### 20.3. Ejecutar Spring Boot

```bash
cd backend-springboot/fleet-monitor
mvn spring-boot:run
```

### 20.4. Ejecutar simulador de sensores IoT

En otra terminal:

```bash
cd python-iot
source .venv/bin/activate
python sensor_simulator.py
```

### 20.5. Consultar API REST

```bash
curl http://localhost:8080/api/fleet/status
curl http://localhost:8080/api/fleet/vehicle/VH-001/telemetria
curl http://localhost:8080/api/fleet/alerts
```

---

## 21. Pruebas rápidas sin simulador

Si se desea probar Spring Boot sin ejecutar todo el flujo MQTT, se puede publicar manualmente un mensaje en RabbitMQ desde la interfaz web.

### 21.1. Mensaje GPS

Cola:

```text
cola.gps.telemetria
```

Payload:

```json
{
  "vehicle_id": "VH-001",
  "timestamp": "2026-05-16T10:30:00",
  "lat": -3.99313,
  "lng": -79.20422,
  "speed": 55.4
}
```

### 21.2. Mensaje de alerta de temperatura

Cola:

```text
cola.alertas.temperatura
```

Payload:

```json
{
  "type": "TEMP_ALERT",
  "message": "Temperatura excedida",
  "vehicle_id": "VH-002",
  "timestamp": "2026-05-16T10:31:00",
  "temperature": 6.8,
  "unit": "celsius"
}
```

### 21.3. Mensaje de combustible bajo

Cola:

```text
cola.combustible.nivel
```

Payload:

```json
{
  "type": "FUEL_LOW",
  "message": "Combustible bajo",
  "vehicle_id": "VH-003",
  "timestamp": "2026-05-16T10:32:00",
  "fuel_level": 14.5,
  "unit": "percent"
}
```

---

## 22. Evidencias esperadas

### 22.1. Logs de Spring Boot

```text
GPS recibido | vehículo=VH-001 | lat=-3.99313 | lng=-79.20422 | velocidad=55.4 km/h
ALERTA TEMPERATURA | vehículo=VH-002 | temperatura=6.8 celsius
ALERTA COMBUSTIBLE | vehículo=VH-003 | nivel=14.5 percent
NOTIFICACIÓN | vehículo=VH-003 | título=Combustible bajo | severidad=MEDIUM | detalle=Nivel de combustible bajo: 14.5 percent
```

### 22.2. RabbitMQ Management

En `http://localhost:15672` se debe verificar:

- Exchange `exchange.fleet`, si se está usando routing keys.
- Cola `cola.gps.telemetria`.
- Cola `cola.alertas.temperatura`.
- Cola `cola.combustible.nivel`.
- Cola `cola.notificaciones`.
- Mensajes entrando y saliendo de las colas.
- Consumidores activos conectados desde Spring Boot.

### 22.3. API REST

```bash
curl http://localhost:8080/api/fleet/status
```

Ejemplo:

```json
{
  "totalVehicles": 3,
  "gpsMessages": 18,
  "activeAlerts": 4,
  "temperatureAlerts": 2,
  "fuelAlerts": 2
}
```

---

## 23. Buenas prácticas obligatorias

1. Separar responsabilidades:
   - `config`: configuración de RabbitMQ.
   - `consumer`: recepción de mensajes.
   - `service`: lógica de negocio.
   - `dto`: estructura de mensajes JSON.
   - `entity`: entidades JPA.
   - `repository`: acceso a datos.
   - `controller`: API REST.

2. No escribir toda la lógica dentro de los consumidores:
   - El consumidor recibe.
   - El servicio procesa.
   - El repositorio guarda.

3. Usar constantes para nombres de colas:
   - Evitar repetir strings como `cola.gps.telemetria` en varias clases.

4. Usar `Logger` en lugar de `System.out.println`:
   - `log.info(...)` para eventos normales.
   - `log.warn(...)` para alertas.
   - `log.error(...)` para errores.

5. Comentar solo lo necesario:
   - Comentar reglas de negocio.
   - Comentar decisiones técnicas importantes.
   - No comentar código obvio.

6. Manejar errores sin detener la aplicación:
   - Si llega un mensaje mal formado, registrar el error y continuar escuchando.

7. Mantener la configuración fuera del código:
   - Usuario, contraseña, host y puerto de RabbitMQ deben estar en `application.yml`.

8. Usar nombres claros:
   - `GpsConsumer`, `AlertConsumer`, `FleetStateService`, `NotificationService`, etc.

---

## 24. Errores comunes y solución

### 24.1. Spring Boot no conecta con RabbitMQ

Revisar:

```bash
docker ps
```

Confirmar credenciales en `application.yml`:

```yaml
spring:
  rabbitmq:
    username: admin
    password: admin123
```

### 24.2. Las colas no aparecen en RabbitMQ

Ejecutar Spring Boot. Las colas se crean cuando carga `RabbitMQConfig`.

```bash
mvn spring-boot:run
```

### 24.3. Spring Boot no consume mensajes

Verificar:

- El nombre de la cola es exactamente igual.
- El bridge publica en la misma cola.
- RabbitMQ está en el puerto `5672`.
- Existe un consumidor activo en RabbitMQ Management.

### 24.4. Error al convertir JSON

Revisar que el JSON use los mismos nombres esperados:

```json
"vehicle_id"
"fuel_level"
```

En Java, esos campos se mapean con:

```java
@JsonProperty("vehicle_id")
@JsonProperty("fuel_level")
```

### 24.5. No se generan alertas

Recordar que el bridge debe publicar alertas solo cuando:

```text
Temperatura > 4 °C
Combustible < 20 %
```

Para probar rápidamente, modificar temporalmente el simulador para generar valores fuera de esos umbrales.

---

## 25. Entrega recomendada

El repositorio final debe contener al menos:

```text
flota-mensajeria-distribuida/
├── README.md
├── docker-compose-rabbitmq.yml
├── python-iot/
│   ├── sensor_simulator.py
│   ├── mqtt_subscriber_storage.py
│   ├── mqtt_rabbitmq_bridge.py
│   └── telemetria.db
└── backend-springboot/
    └── fleet-monitor/
        ├── pom.xml
        └── src/main/...
```

También se recomienda incluir capturas de:

- Contenedor RabbitMQ activo.
- RabbitMQ Management con colas creadas.
- Bridge publicando en RabbitMQ.
- Spring Boot consumiendo GPS y alertas.
- API REST respondiendo en `/api/fleet/status`.
- H2 Console mostrando datos almacenados.

---

## 26. Preguntas de control

### 26.1. Explique los cuatro tipos de exchanges en RabbitMQ y proporcione un ejemplo de uso en el contexto de la flota logística.

- **Direct exchange:** enruta mensajes según una routing key exacta. Ejemplo: enviar GPS con `gps.routing` a `cola.gps.telemetria`.
- **Fanout exchange:** envía el mensaje a todas las colas enlazadas. Ejemplo: difundir una alerta crítica a varias colas de monitoreo, auditoría y notificaciones.
- **Topic exchange:** enruta usando patrones. Ejemplo: `flota.*.gps` para recibir GPS de cualquier vehículo.
- **Headers exchange:** enruta según cabeceras del mensaje. Ejemplo: enviar mensajes según cabecera `severity=HIGH` o `sensorType=temperature`.

### 26.2. ¿Qué es una cola durable en RabbitMQ y por qué es importante?

Una cola durable es una cola que RabbitMQ puede conservar después de un reinicio del broker. Es importante porque evita que la infraestructura de colas desaparezca si RabbitMQ se reinicia. Si las colas no son durables, se perdería su definición y los servicios tendrían que volver a crearlas; además, los mensajes no persistentes podrían perderse.

### 26.3. Compare pub/sub con point-to-point. ¿Cuáles son las ventajas de pub/sub para monitoreo de flotas?

- **Point-to-point:** un mensaje va normalmente a un consumidor de una cola. Es útil para distribuir trabajo.
- **Pub/sub:** un publicador envía eventos y varios suscriptores pueden recibirlos. Es útil cuando GPS, alertas, dashboard y auditoría necesitan reaccionar al mismo evento.

En monitoreo de flotas, pub/sub permite desacoplar sensores, procesamiento, almacenamiento y visualización. Esto facilita agregar nuevos consumidores sin modificar los sensores.

### 26.4. ¿Qué medidas de seguridad se pueden implementar en MQTT y RabbitMQ?

Medidas recomendadas:

- Autenticación con usuarios y contraseñas.
- Autorización por permisos de lectura/escritura en topics, exchanges y colas.
- Cifrado TLS para proteger mensajes en tránsito.
- Control de acceso por roles.
- Deshabilitar usuarios por defecto en ambientes reales.
- Usar redes privadas de Docker o firewall para exponer solo los puertos necesarios.
- Registrar logs de conexión, publicación y consumo.

---

## 27. Resultado final de la Parte 2

La práctica se considera completa cuando se cumple lo siguiente:

- RabbitMQ se ejecuta correctamente en Docker.
- Spring Boot crea y consume las colas RabbitMQ.
- `GpsConsumer` procesa datos de `cola.gps.telemetria`.
- `AlertConsumer` procesa temperatura y combustible.
- `NotificationConsumer` recibe las notificaciones internas.
- Los datos se almacenan en H2 mediante Spring Data JPA.
- La API REST responde correctamente.
- El flujo end-to-end funciona desde el simulador MQTT hasta la API REST.

Con esto se demuestra el uso de RabbitMQ como broker confiable para procesamiento distribuido y Spring Boot como capa de microservicios para monitoreo de flota logística.
