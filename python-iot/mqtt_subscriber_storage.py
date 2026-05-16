"""
Suscriptor MQTT con almacenamiento SQLite.
Recibe telemetría GPS, temperatura y combustible, la almacena en SQLite
y emite alertas en consola cuando se superan los umbrales.
"""

import json
import sqlite3
from datetime import datetime

import paho.mqtt.client as mqtt

# --- Constantes de configuración ---
BROKER_HOST = "localhost"
BROKER_PORT = 1883
DB_PATH = "telemetria.db"

# Umbrales de alerta
TEMP_THRESHOLD = 4.0    # °C: alerta si temperatura > 4
FUEL_THRESHOLD = 20.0   # %:  alerta si combustible < 20

TOPICS = [
    ("flota/+/gps", 0),
    ("flota/+/temperatura", 0),
    ("flota/+/combustible", 0),
]


def inicializar_base_datos(conn: sqlite3.Connection) -> None:
    """Crea las tablas en SQLite si no existen."""
    cursor = conn.cursor()

    cursor.execute("""
        CREATE TABLE IF NOT EXISTS gps_data (
            id        INTEGER PRIMARY KEY AUTOINCREMENT,
            vehicle_id TEXT NOT NULL,
            lat        REAL NOT NULL,
            lng        REAL NOT NULL,
            speed      REAL NOT NULL,
            timestamp  TEXT NOT NULL
        )
    """)

    cursor.execute("""
        CREATE TABLE IF NOT EXISTS temp_data (
            id         INTEGER PRIMARY KEY AUTOINCREMENT,
            vehicle_id TEXT NOT NULL,
            temperature REAL NOT NULL,
            unit       TEXT NOT NULL,
            timestamp  TEXT NOT NULL
        )
    """)

    cursor.execute("""
        CREATE TABLE IF NOT EXISTS fuel_data (
            id         INTEGER PRIMARY KEY AUTOINCREMENT,
            vehicle_id TEXT NOT NULL,
            fuel_level REAL NOT NULL,
            unit       TEXT NOT NULL,
            timestamp  TEXT NOT NULL
        )
    """)

    conn.commit()
    print("Base de datos SQLite lista.")


def guardar_gps(conn: sqlite3.Connection, data: dict) -> None:
    """Inserta un registro GPS en la base de datos."""
    conn.execute(
        "INSERT INTO gps_data (vehicle_id, lat, lng, speed, timestamp) VALUES (?, ?, ?, ?, ?)",
        (data["vehicle_id"], data["lat"], data["lng"], data["speed"], data["timestamp"]),
    )
    conn.commit()


def guardar_temperatura(conn: sqlite3.Connection, data: dict) -> None:
    """Inserta un registro de temperatura en la base de datos."""
    conn.execute(
        "INSERT INTO temp_data (vehicle_id, temperature, unit, timestamp) VALUES (?, ?, ?, ?)",
        (data["vehicle_id"], data["temperature"], data["unit"], data["timestamp"]),
    )
    conn.commit()


def guardar_combustible(conn: sqlite3.Connection, data: dict) -> None:
    """Inserta un registro de combustible en la base de datos."""
    conn.execute(
        "INSERT INTO fuel_data (vehicle_id, fuel_level, unit, timestamp) VALUES (?, ?, ?, ?)",
        (data["vehicle_id"], data["fuel_level"], data["unit"], data["timestamp"]),
    )
    conn.commit()


def on_connect(client, userdata, flags, rc):
    """Callback al conectar: suscribe a los topics de telemetría."""
    if rc == 0:
        print("Suscriptor conectado al broker Mosquitto.")
        client.subscribe(TOPICS)
        print("Suscrito a: flota/+/gps | flota/+/temperatura | flota/+/combustible")
    else:
        print(f"Error al conectar. Código: {rc}")


def on_message(client, userdata, msg):
    """Callback al recibir un mensaje MQTT: lo parsea, almacena y evalúa alertas."""
    conn: sqlite3.Connection = userdata
    topic: str = msg.topic

    try:
        data = json.loads(msg.payload.decode("utf-8"))
    except (json.JSONDecodeError, UnicodeDecodeError) as exc:
        print(f"Error al parsear mensaje de {topic}: {exc}")
        return

    if "/gps" in topic:
        guardar_gps(conn, data)

    elif "/temperatura" in topic:
        guardar_temperatura(conn, data)
        # Alerta si temperatura supera el umbral
        if data.get("temperature", 0) > TEMP_THRESHOLD:
            print(f"ALERTA: {data['vehicle_id']} temperatura alta: {data['temperature']} °C")

    elif "/combustible" in topic:
        guardar_combustible(conn, data)
        # Alerta si combustible está por debajo del umbral
        if data.get("fuel_level", 100) < FUEL_THRESHOLD:
            print(f"ALERTA: {data['vehicle_id']} combustible bajo: {data['fuel_level']} %")


def main():
    """Inicia el suscriptor MQTT y mantiene el bucle de escucha activo."""
    conn = sqlite3.connect(DB_PATH)
    inicializar_base_datos(conn)

    client = mqtt.Client(userdata=conn)
    client.on_connect = on_connect
    client.on_message = on_message

    print("Iniciando suscriptor MQTT con almacenamiento SQLite...")
    client.connect(BROKER_HOST, BROKER_PORT, keepalive=60)

    try:
        client.loop_forever()
    except KeyboardInterrupt:
        print("\nSuscriptor detenido por el usuario.")
        client.disconnect()
        conn.close()


if __name__ == "__main__":
    main()
