# rosradio_scraper

Скрапер для сайта [rosradio-online.ru](https://rosradio-online.ru/stations.htm) — собирает данные о радиостанциях, городах вещания и потоках, заполняя PostgreSQL базу данных.

## Результат скрапинга

| Таблица | Записей | Дубликатов |
|---|---|---|
| `radio_station` | **391** | **0** |
| `city` | **602** | **0** |
| `radio_station_city` | **3 065** связей | — |

## Схема БД

```
┌──────────────────────────────────┐
│  radio_station                    │
├──────────────────────────────────┤
│ id          SERIAL PK            │
│ name        VARCHAR(500) UNIQUE  │
│ description TEXT DEFAULT ''      │
│ imageurl    TEXT DEFAULT ''      │
│ streamurl   TEXT DEFAULT ''      │
│ createdat   TIMESTAMPTZ          │
│ updatedat   TIMESTAMPTZ          │
└──────────┬───────────────────────┘
           │
           │ radio_station_city
           ├──────────────────────────┐
           │ radio_station_id FK      │
           │ city_id FK               │
           │ PK (radio_station_id,     │
           │     city_id)             │
           └──────────┬───────────────┘
                      │
┌─────────────────────┴────────────┐
│  city                             │
├──────────────────────────────────┤
│ id     SERIAL PK                 │
│ name   VARCHAR(500) UNIQUE       │
│ region VARCHAR(500)               │
└──────────────────────────────────┘
```

## Логика выбора streamUrl

Для каждой станции выбирается **один** поток вещания по алгоритму:

1. **🏙 Москва** — если у станции есть город «Москва», берётся лучший поток именно из Москвы (mp3 > aac > m3u8, с максимальным битрейтом)
2. **📍 Ближайший к Москве** — если Москвы нет, выбирается город, ближайший к Москве по координатам (~80 городов в справочнике), и берётся его поток
3. **🔄 Fallback** — если ни у одного города нет потоков — лучший из всех доступных

**Неработающие потоки** (помеченные `link_inactive` / «не работает») полностью отфильтровываются.

## Установка

```bash
cd rosradio_scraper
python3 -m venv venv
source venv/bin/activate
pip install requests psycopg2-binary
```

## Настройка

Отредактируйте `DB_CONFIG` в `scraper.py`:

```python
DB_CONFIG = {
    "host": "158.160.179.250",
    "port": 5432,
    "dbname": "radio_db",
    "user": "postgres",
    "password": "OFqBiZ2-LP",
}
```

## Запуск

```bash
source venv/bin/activate
python3 scraper.py
```

Скрапер:
- Скачивает `stations.htm` → получает список из ~423 станций
- Для каждой скачивает `station_XXXXXX.htm` → парсит города и потоки
- Выбирает лучший поток по алгоритму «Москва → ближайший → fallback»
- Заполняет таблицы PostgreSQL с UNIQUE-ограничениями (без дубликатов)
- Логирует прогресс: `🏙 Выбран поток Москвы` или `📍 Выбран поток ближайшего к Москве города: Рязань (182 км)`

## Примеры SQL-запросов

Все станции в конкретном городе:
```sql
SELECT rs.name, rs.streamurl
FROM radio_station rs
JOIN radio_station_city rsc ON rs.id = rsc.radio_station_id
JOIN city c ON c.id = rsc.city_id
WHERE c.name = 'Москва';
```

Все города для конкретной станции:
```sql
SELECT c.name, c.region
FROM city c
JOIN radio_station_city rsc ON c.id = rsc.city_id
JOIN radio_station rs ON rs.id = rsc.radio_station_id
WHERE rs.name = 'Авторадио';
```

Количество станций по городам (топ-10):
```sql
SELECT c.name, c.region, COUNT(DISTINCT rs.id) AS station_count
FROM city c
JOIN radio_station_city rsc ON c.id = rsc.city_id
JOIN radio_station rs ON rs.id = rsc.radio_station_id
GROUP BY c.name, c.region
ORDER BY station_count DESC
LIMIT 10;
```

Станции с московским потоком:
```sql
SELECT name, streamurl
FROM radio_station
WHERE streamurl LIKE '%23.105.238.4%'
   OR streamurl LIKE '%hostingradio.ru%'
   OR streamurl LIKE '%msk%'
ORDER BY name;
```

## Структура проекта

```
rosradio_scraper/
├── scraper.py    — основной скрипт скрапинга
├── venv/         — виртуальное окружение Python
└── README.md     — документация
```

## Источник данных

[rosradio-online.ru](https://rosradio-online.ru/stations.htm) — сайт Valeriy Kostin, 2023–2026
