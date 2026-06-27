#!/usr/bin/env python3
"""
Работа с PostgreSQL для скрапера радиостанций.

Схема БД:
  - radio_station (id, name, description, imageurl, streamurl, createdat, updatedat)
  - city_image (id, imageurl, downloadurl, source)
  - region_image (id, imageurl, downloadurl, source)
  - city (id, name, region, city_image_id, region_image_id)
  - radio_station_city (radio_station_id, city_id) -- многие-ко-многим
"""

import psycopg2

DB_CONFIG = {
    "host": "158.160.179.250",
    "port": 5432,
    "dbname": "radio_db",
    "user": "postgres",
    "password": "OFqBiZ2-LP",
}

CREATE_TABLES_SQL = """
-- Радиостанции
CREATE TABLE IF NOT EXISTS radio_station (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(500) NOT NULL UNIQUE,
    description TEXT NOT NULL DEFAULT '',
    imageurl    TEXT NOT NULL DEFAULT '',
    streamurl   TEXT NOT NULL DEFAULT '',
    createdat   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updatedat   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Фотографии городов (гербы)
CREATE TABLE IF NOT EXISTS city_image (
    id          SERIAL PRIMARY KEY,
    imageurl    TEXT NOT NULL DEFAULT '',
    downloadurl TEXT NOT NULL DEFAULT '',
    source      TEXT NOT NULL DEFAULT ''
);

-- Фотографии областей (флаги регионов)
CREATE TABLE IF NOT EXISTS region_image (
    id          SERIAL PRIMARY KEY,
    imageurl    TEXT NOT NULL DEFAULT '',
    downloadurl TEXT NOT NULL DEFAULT '',
    source      TEXT NOT NULL DEFAULT ''
);

-- Города
CREATE TABLE IF NOT EXISTS city (
    id              SERIAL PRIMARY KEY,
    name            VARCHAR(500) NOT NULL UNIQUE,
    region          VARCHAR(500),
    city_image_id   INTEGER REFERENCES city_image(id) ON DELETE SET NULL,
    region_image_id INTEGER REFERENCES region_image(id) ON DELETE SET NULL
);

-- Связь многие-ко-многим: радиостанция <-> город
CREATE TABLE IF NOT EXISTS radio_station_city (
    radio_station_id INTEGER NOT NULL REFERENCES radio_station(id) ON DELETE CASCADE,
    city_id          INTEGER NOT NULL REFERENCES city(id) ON DELETE CASCADE,
    PRIMARY KEY (radio_station_id, city_id)
);

-- Индексы для быстрого поиска
CREATE INDEX IF NOT EXISTS idx_radio_station_name ON radio_station(name);
CREATE INDEX IF NOT EXISTS idx_city_name ON city(name);
CREATE INDEX IF NOT EXISTS idx_rsc_radio ON radio_station_city(radio_station_id);
CREATE INDEX IF NOT EXISTS idx_rsc_city ON radio_station_city(city_id);
CREATE INDEX IF NOT EXISTS idx_city_image_fk ON city(city_image_id);
CREATE INDEX IF NOT EXISTS idx_region_image_fk ON city(region_image_id);
"""

# SQL для миграции существующей БД (добавление новых таблиц и колонок)
MIGRATION_SQL = """
-- Создать таблицы city_image и region_image, если их нет
CREATE TABLE IF NOT EXISTS city_image (
    id          SERIAL PRIMARY KEY,
    imageurl    TEXT NOT NULL DEFAULT '',
    downloadurl TEXT NOT NULL DEFAULT '',
    source      TEXT NOT NULL DEFAULT ''
);

CREATE TABLE IF NOT EXISTS region_image (
    id          SERIAL PRIMARY KEY,
    imageurl    TEXT NOT NULL DEFAULT '',
    downloadurl TEXT NOT NULL DEFAULT '',
    source      TEXT NOT NULL DEFAULT ''
);

-- Добавить колонки city_image_id и region_image_id в таблицу city, если их нет
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'city' AND column_name = 'city_image_id'
    ) THEN
        ALTER TABLE city ADD COLUMN city_image_id INTEGER REFERENCES city_image(id) ON DELETE SET NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'city' AND column_name = 'region_image_id'
    ) THEN
        ALTER TABLE city ADD COLUMN region_image_id INTEGER REFERENCES region_image(id) ON DELETE SET NULL;
    END IF;
END $$;

-- Создать индексы, если их нет
CREATE INDEX IF NOT EXISTS idx_city_image_fk ON city(city_image_id);
CREATE INDEX IF NOT EXISTS idx_region_image_fk ON city(region_image_id);
"""


def get_connection() -> psycopg2.extensions.connection:
    """Создаёт подключение к PostgreSQL."""
    return psycopg2.connect(**DB_CONFIG)


def init_db(conn):
    """Создаёт таблицы, если их нет.

    Сначала выполняет миграцию (добавление новых колонок в существующие таблицы),
    затем — создание всех таблиц с нуля (для свежей БД).
    """
    with conn.cursor() as cur:
        # Миграция: добавить новые колонки, если таблицы уже существуют
        cur.execute(MIGRATION_SQL)
        # Создание таблиц с нуля (IF NOT EXISTS — безопасно для существующих)
        cur.execute(CREATE_TABLES_SQL)
    conn.commit()


def upsert_station(
    conn, name: str, stream_url: str, description: str = "", image_url: str = ""
) -> int | None:
    """Вставляет радиостанцию или возвращает id существующей (по UNIQUE name).

    Колонки таблицы: imageurl, streamurl, createdat, updatedat.
    """
    from datetime import datetime, timezone
    now = datetime.now(timezone.utc)
    with conn.cursor() as cur:
        cur.execute(
            """
            INSERT INTO radio_station (name, description, imageurl, streamurl, createdat, updatedat)
            VALUES (%s, %s, %s, %s, %s, %s)
            ON CONFLICT (name) DO NOTHING
            RETURNING id
            """,
            (name, description, image_url, stream_url, now, now),
        )
        row = cur.fetchone()
        if row:
            return row[0]
        # Конфликт по name -- получаем существующий id
        cur.execute("SELECT id FROM radio_station WHERE name = %s", (name,))
        row = cur.fetchone()
        return row[0] if row else None


def upsert_city(
    conn, name: str, region: str | None = None,
    city_image_id: int | None = None, region_image_id: int | None = None,
) -> int | None:
    """Вставляет город или возвращает id существующего (по UNIQUE name).

    Если город уже есть -- обновляет регион, если он был пустой (COALESCE).
    Также обновляет city_image_id и region_image_id, если они переданы.
    """
    with conn.cursor() as cur:
        cur.execute(
            """
            INSERT INTO city (name, region, city_image_id, region_image_id)
            VALUES (%s, %s, %s, %s)
            ON CONFLICT (name) DO UPDATE SET
                region = COALESCE(city.region, EXCLUDED.region),
                city_image_id = COALESCE(EXCLUDED.city_image_id, city.city_image_id),
                region_image_id = COALESCE(EXCLUDED.region_image_id, city.region_image_id)
            RETURNING id
            """,
            (name, region, city_image_id, region_image_id),
        )
        row = cur.fetchone()
        return row[0] if row else None


def link_station_city(conn, station_id: int, city_id: int):
    """Создаёт связь радиостанция <-> город (если её ещё нет)."""
    with conn.cursor() as cur:
        cur.execute(
            """
            INSERT INTO radio_station_city (radio_station_id, city_id)
            VALUES (%s, %s)
            ON CONFLICT DO NOTHING
            """,
            (station_id, city_id),
        )


def update_station_image(conn, station_id: int, image_url: str):
    """Обновляет imageurl для существующей станции."""
    from datetime import datetime, timezone
    now = datetime.now(timezone.utc)
    with conn.cursor() as cur:
        cur.execute(
            """
            UPDATE radio_station
            SET imageurl = %s, updatedat = %s
            WHERE id = %s
            """,
            (image_url, now, station_id),
        )


def get_stations_without_image(conn) -> list[tuple[int, str]]:
    """Возвращает список станций без картинки (imageurl = '').

    Returns:
        Список кортежей (id, name).
    """
    with conn.cursor() as cur:
        cur.execute(
            "SELECT id, name FROM radio_station WHERE imageurl = '' OR imageurl IS NULL"
        )
        return cur.fetchall()


def update_station_description(conn, station_id: int, description: str):
    """Обновляет description для существующей станции."""
    from datetime import datetime, timezone
    now = datetime.now(timezone.utc)
    with conn.cursor() as cur:
        cur.execute(
            """
            UPDATE radio_station
            SET description = %s, updatedat = %s
            WHERE id = %s
            """,
            (description, now, station_id),
        )


def get_stations_without_description(conn) -> list[tuple[int, str]]:
    """Возвращает список станций без описания (description = '').

    Returns:
        Список кортежей (id, name).
    """
    with conn.cursor() as cur:
        cur.execute(
            "SELECT id, name FROM radio_station WHERE description = '' OR description IS NULL"
        )
        return cur.fetchall()


def get_all_stations(conn) -> list[tuple[int, str, str]]:
    """Возвращает все станции.

    Returns:
        Список кортежей (id, name, imageurl).
    """
    with conn.cursor() as cur:
        cur.execute("SELECT id, name, imageurl FROM radio_station ORDER BY id")
        return cur.fetchall()


# ─── Работа с city_image и region_image ──────────────────────────────────────


def insert_city_image(conn, image_url: str, download_url: str, source: str = "") -> int | None:
    """Вставляет запись о гербе города в таблицу city_image.

    Returns:
        id вставленной записи или None.
    """
    with conn.cursor() as cur:
        cur.execute(
            """
            INSERT INTO city_image (imageurl, downloadurl, source)
            VALUES (%s, %s, %s)
            RETURNING id
            """,
            (image_url, download_url, source),
        )
        row = cur.fetchone()
        return row[0] if row else None


def insert_region_image(conn, image_url: str, download_url: str, source: str = "") -> int | None:
    """Вставляет запись о флаге региона в таблицу region_image.

    Returns:
        id вставленной записи или None.
    """
    with conn.cursor() as cur:
        cur.execute(
            """
            INSERT INTO region_image (imageurl, downloadurl, source)
            VALUES (%s, %s, %s)
            RETURNING id
            """,
            (image_url, download_url, source),
        )
        row = cur.fetchone()
        return row[0] if row else None


def get_city_image_by_source(conn, source: str) -> int | None:
    """Возвращает id записи city_image по полю source (URL страницы).

    Используется для проверки, не скачивали ли мы уже герб с этой страницы.
    """
    with conn.cursor() as cur:
        cur.execute(
            "SELECT id FROM city_image WHERE source = %s LIMIT 1",
            (source,),
        )
        row = cur.fetchone()
        return row[0] if row else None


def get_region_image_by_source(conn, source: str) -> int | None:
    """Возвращает id записи region_image по полю source (URL страницы).

    Используется для проверки, не скачивали ли мы уже флаг с этой страницы.
    """
    with conn.cursor() as cur:
        cur.execute(
            "SELECT id FROM region_image WHERE source = %s LIMIT 1",
            (source,),
        )
        row = cur.fetchone()
        return row[0] if row else None


def update_city_image_ids(conn, city_id: int, city_image_id: int | None, region_image_id: int | None):
    """Обновляет city_image_id и region_image_id для существующего города."""
    with conn.cursor() as cur:
        cur.execute(
            """
            UPDATE city
            SET city_image_id = COALESCE(%s, city_image_id),
                region_image_id = COALESCE(%s, region_image_id)
            WHERE id = %s
            """,
            (city_image_id, region_image_id, city_id),
        )


def get_cities_without_images(conn) -> list[tuple[int, str, str | None]]:
    """Возвращает список городов без герба (city_image_id IS NULL).

    Returns:
        Список кортежей (id, name, region).
    """
    with conn.cursor() as cur:
        cur.execute(
            "SELECT id, name, region FROM city WHERE city_image_id IS NULL"
        )
        return cur.fetchall()


def get_cities_without_region_image(conn) -> list[tuple[int, str, str | None]]:
    """Возвращает список городов без флага региона (region_image_id IS NULL).

    Returns:
        Список кортежей (id, name, region).
    """
    with conn.cursor() as cur:
        cur.execute(
            "SELECT id, name, region FROM city WHERE region_image_id IS NULL"
        )
        return cur.fetchall()
