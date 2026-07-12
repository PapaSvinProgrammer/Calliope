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
    "host": "89.169.182.128",
    "port": 5432,
    "dbname": "radio_db",
    "user": "postgres",
    "password": "OFqBiZ2-LP",
    # Отключаем GSSAPI-аутентификацию, иначе при отсутствии krb5-конфига
    # psycopg2 падает с "could not initiate GSSAPI security context".
    "gssencmode": "disable",
    # TCP-keepalive: не даёт соединению простаивать часами и отваливаться
    # по таймауту (SSL SYSCALL error: Operation timed out) при долгом скрапинге.
    "connect_timeout": 10,        # секунд на установку соединения
    "keepalives": 1,              # включить TCP keepalive
    "keepalives_idle": 30,        # начинать проверку через 30с простоя
    "keepalives_interval": 10,    # интервал между проверками
    "keepalives_count": 3,        # число неудачных проверок до разрыва
    # Параметры сессии (SET при подключении):
    #   lock_timeout=30s — CREATE TABLE/ALTER TABLE не висят вечно, ожидая блокировку
    #     от зависшей транзакции (была проблема: orphaned tx держала RowExclusiveLock
    #     на city, и init_db зависал на ShareLock).
    #   idle_in_transaction_session_timeout=60s — открытая транзакция без активности
    #     авто-закрывается, чтобы не плодить orphaned-транзакции при падении скрипта.
    "options": "-c lock_timeout=30000 -c idle_in_transaction_session_timeout=60000",
}

# Параметры для подключения к служебной БД postgres (нужна для CREATE DATABASE).
ADMIN_DB_CONFIG = {**DB_CONFIG, "dbname": "postgres"}

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


def create_database_if_not_exists():
    """Создаёт базу данных radio_db, если её ещё нет на сервере.

    Подключается к служебной БД `postgres` (autocommit обязателен для
    CREATE DATABASE) и выполняет CREATE DATABASE IF NOT EXISTS-эквивалент.
    """
    conn = psycopg2.connect(**ADMIN_DB_CONFIG)
    try:
        conn.autocommit = True  # CREATE DATABASE нельзя выполнять в транзакции
        with conn.cursor() as cur:
            cur.execute("SELECT 1 FROM pg_database WHERE datname = %s", (DB_CONFIG["dbname"],))
            exists = cur.fetchone() is not None
            if not exists:
                cur.execute(f'CREATE DATABASE "{DB_CONFIG["dbname"]}"')
                print(f"[db] База данных '{DB_CONFIG['dbname']}' создана")
            else:
                print(f"[db] База данных '{DB_CONFIG['dbname']}' уже существует")
    finally:
        conn.close()


def get_connection() -> psycopg2.extensions.connection:
    """Создаёт подключение к PostgreSQL.

    Если целевая БД ещё не существует — предварительно создаёт её,
    подключаясь к служебной БД `postgres`.
    """
    try:
        return psycopg2.connect(**DB_CONFIG)
    except psycopg2.OperationalError as e:
        # Если БД не существует — создаём и подключаемся заново
        if 'does not exist' in str(e) and DB_CONFIG["dbname"] in str(e):
            create_database_if_not_exists()
            return psycopg2.connect(**DB_CONFIG)
        raise


def reconnect(conn) -> psycopg2.extensions.connection:
    """Безопасно закрывает старое подключение и открывает новое.

    Используется при восстановлении после обрыва связи (Operation timed out).
    """
    try:
        conn.close()
    except Exception:
        pass
    return get_connection()


def with_retry(fn, conn, *args, max_retries: int = 3, **kwargs):
    """Выполняет функцию fn(conn, ...) с автоматическим переподключением.

    При psycopg2.OperationalError (обрыв связи, таймаут) — переподключается
    и повторяет попытку. Возвращает (result, conn) — conn может измениться
    после переподключения, поэтому вызывающая сторона должна использовать
    возвращённое подключение.
    """
    import time as _time
    last_err = None
    for attempt in range(1, max_retries + 1):
        try:
            return fn(conn, *args, **kwargs), conn
        except psycopg2.OperationalError as e:
            last_err = e
            print(f"[db] обрыв соединения (попытка {attempt}/{max_retries}): {e}")
            try:
                conn.rollback()
            except Exception:
                pass
            conn = reconnect(conn)
            _time.sleep(2 * attempt)
    raise last_err


def init_db(conn, max_retries: int = 5):
    """Создаёт таблицы, если их нет, и применяет миграции к существующим.

    Порядок:
      1. CREATE_TABLES_SQL — создаёт все таблицы с нуля (IF NOT EXISTS).
         На свежей БД этого достаточно: таблицы сразу создаются со всеми
         нужными колонками (city_image_id, region_image_id и т.д.).
      2. MIGRATION_SQL — добавляет недостающие колонки в уже существующие
         таблицы (для БД, созданных предыдущими версиями скрипта).

    При LockNotAvailable (lock_timeout сработал из-за чужой транзакции,
    держащей блокировку) — повторяет попытку с задержкой.
    """
    import time as _time
    for attempt in range(1, max_retries + 1):
        try:
            with conn.cursor() as cur:
                # Сначала создаём все таблицы (безопасно — IF NOT EXISTS)
                cur.execute(CREATE_TABLES_SQL)
                # Затем — миграция старых таблиц: добавляем колонки, если их нет
                cur.execute(MIGRATION_SQL)
            conn.commit()
            return
        except psycopg2.OperationalError as e:
            # LockNotAvailable — subclass OperationalError (через QueryCanceled)
            if "lock timeout" in str(e).lower() or "locknotavailable" in str(e).lower().replace(" ", ""):
                print(f"[db] lock timeout в init_db (попытка {attempt}/{max_retries}): {e}")
                try:
                    conn.rollback()
                except Exception:
                    pass
                if attempt < max_retries:
                    _time.sleep(5 * attempt)
                    continue
            raise


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
