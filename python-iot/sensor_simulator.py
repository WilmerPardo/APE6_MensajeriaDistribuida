"""
Simulador de sensores IoT para el Sistema de Monitoreo de Flota Logística.
Publica datos de GPS, temperatura y combustible via MQTT para tres vehículos.
"""

import json
import random
import time
from datetime import datetime

import paho.mqtt.client as mqtt

# --- Constantes de configuración ---
BROKER_HOST = "localhost"
BROKER_PORT = 1883
PUBLISH_INTERVAL = 5  # segundos entre publicaciones

VEHICULOS = ["VH-001", "VH-002", "VH-003"]

# Coordenadas base para simular movimiento (Guayaquil, Ecuador)
GPS_BASE = {
    "VH-001": {"lat": -2.1709, "lng": -79.9224},
    "VH-002": {"lat": -2.1850, "lng": -79.9000},
    "VH-003": {"lat": -2.1600, "lng": -79.9400},
}


def on_connect(client, userdata, flags, rc):
    """Callback ejecutado al conectarse al broker MQTT."""
    if rc == 0:
        print("Conectado al broker Mosquitto correctamente.")
    else:
        print(f"Error al conectar. Código: {rc}")


def simular_gps(id_vehiculo: str) -> dict:
    """
    Genera datos GPS simulados para un vehículo.
    Las coordenadas varían ligeramente respecto a la base para simular movimiento.
    """
    base = GPS_BASE[id_vehiculo]
    return {
        "vehicle_id": id_vehiculo,
        "timestamp": datetime.now().isoformat(timespec="seconds"),
        "lat": round(base["lat"] + random.uniform(-0.005, 0.005), 6),
        "lng": round(base["lng"] + random.uniform(-0.005, 0.005), 6),
        "speed": round(random.uniform(20.0, 90.0), 1),
    }


def simular_temperatura(id_vehiculo: str) -> dict:
    """
    Genera datos de temperatura simulados para un vehículo.
    El umbral de alerta es temperatura > 4 °C.
    """
    return {
        "vehicle_id": id_vehiculo,
        "timestamp": datetime.now().isoformat(timespec="seconds"),
        "temperature": round(random.uniform(-2.0, 10.0), 1),
        "unit": "celsius",
    }


def simular_combustible(id_vehiculo: str) -> dict:
    """
    Genera datos de nivel de combustible simulados para un vehículo.
    El umbral de alerta es fuel_level < 20 %.
    """
    return {
        "vehicle_id": id_vehiculo,
        "timestamp": datetime.now().isoformat(timespec="seconds"),
        "fuel_level": round(random.uniform(5.0, 100.0), 1),
        "unit": "percent",
    }


def main():
    """Inicia el simulador de sensores IoT y publica datos periódicamente."""
    client = mqtt.Client()
    client.on_connect = on_connect

    print("Iniciando simulador de sensores IoT...")
    client.connect(BROKER_HOST, BROKER_PORT, keepalive=60)
    client.loop_start()

    try:
        while True:
            for vehiculo in VEHICULOS:
                # Publicar GPS
                gps_data = simular_gps(vehiculo)
                topic_gps = f"flota/{vehiculo}/gps"
                client.publish(topic_gps, json.dumps(gps_data))

                # Publicar temperatura
                temp_data = simular_temperatura(vehiculo)
                topic_temp = f"flota/{vehiculo}/temperatura"
                client.publish(topic_temp, json.dumps(temp_data))

                # Publicar combustible
                fuel_data = simular_combustible(vehiculo)
                topic_fuel = f"flota/{vehiculo}/combustible"
                client.publish(topic_fuel, json.dumps(fuel_data))

                print(f"[{datetime.now().isoformat(timespec='seconds')}] Datos publicados para {vehiculo}")

            time.sleep(PUBLISH_INTERVAL)

    except KeyboardInterrupt:
        print("\nSimulador detenido por el usuario.")
        client.loop_stop()
        client.disconnect()


if __name__ == "__main__":
    main()
