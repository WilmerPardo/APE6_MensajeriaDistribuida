"""
Servicio puente MQTT → RabbitMQ.
Se suscribe a todos los topics de la flota, filtra alertas y las publica
en las colas correspondientes de RabbitMQ como mensajes persistentes.
"""

import json

import paho.mqtt.client as mqtt
import pika

# --- Constantes de configuración ---
MQTT_HOST = "localhost"
MQTT_PORT = 1883

RABBITMQ_HOST = "localhost"
RABBITMQ_PORT = 5672
RABBITMQ_USER = "guest"
RABBITMQ_PASS = "guest"

# Nombres de las colas RabbitMQ
QUEUE_GPS = "cola.gps.telemetria"
QUEUE_TEMP = "cola.alertas.temperatura"
QUEUE_FUEL = "cola.combustible.nivel"
QUEUE_NOTIF = "cola.notificaciones"

# Umbrales de alerta
TEMP_THRESHOLD = 4.0    # °C: publicar en cola si temperatura > 4
FUEL_THRESHOLD = 20.0   # %:  publicar en cola si combustible < 20


def crear_canal_rabbitmq():
    """
    Crea y devuelve la conexión y canal de RabbitMQ con las colas declaradas como durables.
    Las colas durables sobreviven a reinicios del broker.
    """
    credentials = pika.PlainCredentials(RABBITMQ_USER, RABBITMQ_PASS)
    parameters = pika.ConnectionParameters(
        host=RABBITMQ_HOST,
        port=RABBITMQ_PORT,
        credentials=credentials,
    )
    connection = pika.BlockingConnection(parameters)
    channel = connection.channel()

    # Declarar colas durables
    for queue_name in [QUEUE_GPS, QUEUE_TEMP, QUEUE_FUEL, QUEUE_NOTIF]:
        channel.queue_declare(queue=queue_name, durable=True)

    print("Colas RabbitMQ creadas exitosamente")
    return connection, channel


def publicar_en_rabbitmq(channel, queue: str, payload: dict) -> None:
    """
    Publica un mensaje JSON en la cola RabbitMQ indicada.
    delivery_mode=2 hace el mensaje persistente ante reinicios del broker.
    """
    channel.basic_publish(
        exchange="",
        routing_key=queue,
        body=json.dumps(payload),
        properties=pika.BasicProperties(
            delivery_mode=2,         # Mensaje persistente
            content_type="application/json",
        ),
    )
    print(f"Publicado en {queue}: vehículo={payload.get('vehicle_id', '?')}")


def on_connect(client, userdata, flags, rc):
    """Callback al conectar al broker MQTT: suscribe al wildcard de toda la flota."""
    if rc == 0:
        print("Bridge conectado al broker Mosquitto.")
        client.subscribe("flota/#")
        print("Bridge MQTT-RabbitMQ iniciado...")
    else:
        print(f"Error al conectar al broker MQTT. Código: {rc}")


def on_message(client, userdata, msg):
    """
    Callback al recibir un mensaje MQTT.
    Filtra y publica en RabbitMQ según las reglas de enrutamiento:
      - GPS: siempre se publica en cola.gps.telemetria
      - Temperatura > 4 °C: alerta en cola.alertas.temperatura
      - Combustible < 20 %: alerta en cola.combustible.nivel
    """
    channel = userdata
    topic: str = msg.topic

    try:
        data = json.loads(msg.payload.decode("utf-8"))
    except (json.JSONDecodeError, UnicodeDecodeError) as exc:
        print(f"Error al parsear mensaje de {topic}: {exc}")
        return

    if "/gps" in topic:
        # GPS: siempre publicar telemetría
        publicar_en_rabbitmq(channel, QUEUE_GPS, data)

    elif "/temperatura" in topic:
        # Temperatura: solo publicar si supera el umbral
        if data.get("temperature", 0) > TEMP_THRESHOLD:
            alerta = {
                "type": "TEMP_ALERT",
                "message": "Temperatura excedida",
                "vehicle_id": data["vehicle_id"],
                "timestamp": data["timestamp"],
                "temperature": data["temperature"],
                "unit": data["unit"],
            }
            publicar_en_rabbitmq(channel, QUEUE_TEMP, alerta)
            print(f"Publicado alerta de temperatura en {QUEUE_TEMP}")

    elif "/combustible" in topic:
        # Combustible: solo publicar si está por debajo del umbral
        if data.get("fuel_level", 100) < FUEL_THRESHOLD:
            alerta = {
                "type": "FUEL_LOW",
                "message": "Combustible bajo",
                "vehicle_id": data["vehicle_id"],
                "timestamp": data["timestamp"],
                "fuel_level": data["fuel_level"],
                "unit": data["unit"],
            }
            publicar_en_rabbitmq(channel, QUEUE_FUEL, alerta)
            print(f"Publicado alerta de combustible en {QUEUE_FUEL}")


def main():
    """Inicia el servicio puente MQTT-RabbitMQ."""
    # Conectar a RabbitMQ primero
    rabbitmq_conn, channel = crear_canal_rabbitmq()

    # Conectar a MQTT pasando el canal como userdata
    client = mqtt.Client(userdata=channel)
    client.on_connect = on_connect
    client.on_message = on_message

    client.connect(MQTT_HOST, MQTT_PORT, keepalive=60)

    try:
        client.loop_forever()
    except KeyboardInterrupt:
        print("\nBridge detenido por el usuario.")
        client.disconnect()
        rabbitmq_conn.close()


if __name__ == "__main__":
    main()
