#!/usr/bin/env python3
"""
Скрапер для rosradio-online.ru
Собирает данные о радиостанциях, городах и потоках вещания,
заполняет PostgreSQL базу данных.

Схема БД:
  - radio_station (id, name, description, imageurl, streamurl, createdat, updatedat)
  - city (id, name, region)
  - radio_station_city (radio_station_id, city_id) — многие-ко-многим

Логика выбора streamUrl:
  1. Если у станции есть город «Москва» — берём лучший поток из Москвы
  2. Если Москвы нет — берём город, ближайший к Москве (по координатам)
  3. Fallback — лучший из всех рабочих потоков

Использование:
  1. Создайте venv: python3 -m venv venv && source venv/bin/activate
  2. Установите зависимости: pip install requests psycopg2-binary
  3. Настройте подключение к PostgreSQL в переменной DB_CONFIG
  4. Запустите: python3 scraper.py
"""

import re
import time
import logging
from math import radians, sin, cos, sqrt, asin
from urllib.parse import urljoin
from html.parser import HTMLParser

import requests

from db import (
    get_connection,
    init_db,
    upsert_station,
    upsert_city,
    link_station_city,
    update_station_image,
    get_stations_without_image,
    update_station_description,
    get_stations_without_description,
    insert_city_image,
    insert_region_image,
    get_city_image_by_source,
    get_region_image_by_source,
    update_city_image_ids,
    get_cities_without_images,
    get_cities_without_region_image,
)
from image_search import search_image_url, search_image_url_fallback
from description_search import search_description

# ─── Настройки ───────────────────────────────────────────────────────────────

BASE_URL = "https://rosradio-online.ru"
STATIONS_LIST_URL = f"{BASE_URL}/stations.htm"

PH4_BASE_URL = "https://www.ph4.ru"
PH4_CITIES_LIST_URL = f"{PH4_BASE_URL}/h_index.php?y=12&int=l"
PH4_REGIONS_LIST_URL = f"{PH4_BASE_URL}/h_index.php?y=5&int=l"

# Задержка между запросами (секунды) -- чтобы не перегружать сервер
REQUEST_DELAY = 0.5

# Таймаут HTTP-запроса (секунды)
REQUEST_TIMEOUT = 15

# User-Agent для обхода защиты от ботов
HEADERS = {
    "User-Agent": (
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) "
        "AppleWebKit/537.36 (KHTML, like Gecko) "
        "Chrome/126.0.0.0 Safari/537.36"
    ),
    "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
    "Accept-Language": "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7",
    "Accept-Encoding": "gzip, deflate",
    "Connection": "keep-alive",
}

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
)
log = logging.getLogger(__name__)


# ─── География: координаты и расстояния ───────────────────────────────────────

MOSCOW_LAT = 55.7558
MOSCOW_LON = 37.6173

# Координаты городов России (широта, долгота) — для расчёта расстояния от Москвы
CITY_COORDS: dict[str, tuple[float, float]] = {
    "москва": (55.7558, 37.6173),
    "санкт-петербург": (59.9343, 30.3351),
    "новосибирск": (55.0084, 82.9357),
    "екатеринбург": (56.8389, 60.6057),
    "нижний новгород": (56.2965, 43.9361),
    "казань": (55.7887, 49.1221),
    "самара": (53.1959, 50.1500),
    "омск": (54.9885, 73.3242),
    "челябинск": (55.1644, 61.4368),
    "ростов-на-дону": (57.6265, 39.8845),
    "уфа": (54.7388, 55.9721),
    "красноярск": (56.0153, 92.8932),
    "воронеж": (51.6720, 39.1843),
    "пермь": (58.0105, 56.2502),
    "волгоград": (48.7080, 44.5133),
    "краснодар": (45.0355, 38.9753),
    "тюмень": (57.1553, 65.5618),
    "ижевск": (56.8526, 53.2117),
    "барнаул": (53.3481, 83.7798),
    "ульяновск": (54.3333, 48.2667),
    "иркутск": (52.2970, 104.7057),
    "хабаровск": (48.4827, 135.0838),
    "владивосток": (43.1155, 131.8855),
    "тверь": (56.8621, 35.9006),
    "рязань": (54.6266, 39.6925),
    "тольятти": (53.5078, 49.4203),
    "калининград": (54.7104, 20.4522),
    "ставрополь": (45.0428, 41.9734),
    "саранск": (54.1838, 45.1749),
    "симферополь": (44.9521, 34.1024),
    "киров": (58.5966, 49.6618),
    "севастополь": (44.6166, 33.5254),
    "мурманск": (68.9585, 33.0827),
    "саратов": (51.5331, 46.0341),
    "кемерово": (55.3559, 86.0877),
    "астрахань": (46.3499, 48.0401),
    "смоленск": (54.7806, 32.0455),
    "брянск": (53.2435, 34.3637),
    "пенза": (53.1956, 45.0235),
    "калуга": (54.5138, 36.2634),
    "орел": (52.9685, 36.0755),
    "тамбов": (52.7217, 41.4523),
    "вологда": (59.2231, 39.8820),
    "курск": (51.7306, 36.1926),
    "липецк": (52.6107, 39.5947),
    "белгород": (50.5957, 36.5802),
    "владимир": (56.1290, 40.4066),
    "иваново": (56.9972, 40.9714),
    "кострома": (57.7679, 40.9294),
    "тула": (54.1961, 37.6182),
    "ярославль": (57.6265, 39.8845),
    "архангельск": (64.5394, 40.5169),
    "петрозаводск": (61.7849, 34.3469),
    "сыктывкар": (61.6686, 50.8334),
    "йошкар-ола": (56.6343, 47.8997),
    "чебоксары": (56.1439, 47.2488),
    "назрань": (43.2733, 44.2227),
    "магас": (43.1566, 44.1619),
    "грозный": (43.3180, 45.6832),
    "майкоп": (44.6098, 40.1006),
    "нальчик": (43.4977, 43.6187),
    "владикавказ": (43.0324, 44.6789),
    "махачкала": (42.9830, 47.3374),
    "элиста": (46.3088, 44.2616),
    "черкесск": (44.2233, 42.0578),
    "горно-алтайск": (52.0311, 85.9447),
    "абакан": (53.7156, 91.4291),
    "кызыл": (51.7139, 94.4367),
    "ханты-мансийск": (61.0043, 69.0176),
    "салехард": (66.5300, 66.6026),
    "анадырь": (64.7340, 177.5139),
    "петропавловск-камчатский": (53.0106, 158.6506),
    "южно-сахалинск": (46.9591, 142.7383),
    "якутск": (62.0355, 129.7423),
    "биробиджан": (48.7933, 132.9263),
    "магадан": (59.5639, 150.8039),
    "благовещенск": (50.2903, 127.5274),
    "чита": (52.0335, 113.5025),
    "улан-удэ": (51.8333, 107.5833),
    "норильск": (69.3492, 88.2013),
    "сургут": (61.2540, 73.3962),
    "нижневартовск": (60.9388, 76.5727),
    "новокузнецк": (53.7623, 87.1087),
    "оренбург": (51.7730, 55.0987),
    "псков": (57.8190, 28.3318),
    "великий новгород": (58.5219, 31.2726),
}

# Расстояние для неизвестных городов (очень большое — будет в конце списка)
_UNKNOWN_CITY_DISTANCE = 99999.0


def haversine_km(lat1: float, lon1: float, lat2: float, lon2: float) -> float:
    """Расстояние между двумя точками на Земле в км (формула Гаверсинуса)."""
    R = 6371.0
    dlat = radians(lat2 - lat1)
    dlon = radians(lon2 - lon1)
    a = sin(dlat / 2) ** 2 + cos(radians(lat1)) * cos(radians(lat2)) * sin(dlon / 2) ** 2
    c = 2 * asin(sqrt(a))
    return R * c


def city_distance_from_moscow(city_name: str) -> float:
    """Возвращает расстояние города от Москвы в км.
    Если город неизвестен — возвращает _UNKNOWN_CITY_DISTANCE.
    """
    key = city_name.lower().strip()
    if key in CITY_COORDS:
        lat, lon = CITY_COORDS[key]
        return haversine_km(MOSCOW_LAT, MOSCOW_LON, lat, lon)
    return _UNKNOWN_CITY_DISTANCE


# ─── HTML-парсер для списка станций ─────────────────────────────────────────

class StationListParser(HTMLParser):
    """Парсит страницу stations.htm и извлекает ссылки на страницы станций."""

    def __init__(self):
        super().__init__()
        self.stations: list[tuple[str, str]] = []  # (href, name)
        self._current_href: str | None = None
        self._in_link: bool = False

    def handle_starttag(self, tag, attrs):
        if tag != "a":
            return
        attrs_dict = dict(attrs)
        href = attrs_dict.get("href", "")
        css_class = attrs_dict.get("class", "")

        if href.startswith("station_") and href.endswith(".htm") and css_class == "link":
            self._current_href = href
            self._in_link = True

    def handle_data(self, data):
        if self._in_link and self._current_href:
            name = data.strip()
            if name:
                self.stations.append((self._current_href, name))

    def handle_endtag(self, tag):
        if tag == "a" and self._in_link:
            self._in_link = False
            self._current_href = None


# ─── Парсинг страницы станции ────────────────────────────────────────────────

# Приоритет форматов при выборе лучшего потока
FORMAT_PRIORITY: dict[str, int] = {"mp3": 0, "aac": 1, "m3u8": 2, "ogg": 3, "opus": 4}

# Регулярка для извлечения вызовов PrintLink(...) из HTML
_PRINTLINK_RE = re.compile(
    r"PrintLink\s*\(\s*"
    r"'([^']+)'\s*,\s*"      # group 1: URL
    r"'([^']+)'\s*,\s*"      # group 2: format
    r"'([^']*)'\s*,\s*"      # group 3: ssl (не используется)
    r"'([^']*)'\s*,\s*"      # group 4: style (link / link_inactive)
    r"([^)]*?)\s*"           # group 5: bitrate
    r"\)"
)

# Регулярка для строк таблицы С регионом
_ROW_WITH_REGION_RE = re.compile(
    r'<tr>\s*'
    r'<td\s+class=streams[01]>\s*\d+\s*</td>\s*'
    r'<td\s+class=streams[01]>'                            # ячейка города
    r'([^<]+?)'                                             # group 1: название города
    r'\s*<font\s+class=obl>\s*\(([^)]+?)\)\s*</font>'       # group 2: регион
    r'(?:<font\s+class=invisible>[^<]*</font>)?'            # невидимая частота
    r'\s*</td>\s*'
    r'<td\s+class=streams[01]>'                             # ячейка потоков
    r'(.*?)'                                                # group 3: содержимое потоков
    r'</td>',
    re.DOTALL,
)

# Регулярка для строк таблицы БЕЗ региона (Москва, Санкт-Петербург и т.п.)
_ROW_WITHOUT_REGION_RE = re.compile(
    r'<tr>\s*'
    r'<td\s+class=streams[01]>\s*\d+\s*</td>\s*'
    r'<td\s+class=streams[01]>'                            # ячейка города
    r'([^<]+?)'                                             # group 1: название города
    r'(?:<font\s+class=invisible>[^<]*</font>)?'            # невидимая частота (без региона!)
    r'\s*</td>\s*'
    r'<td\s+class=streams[01]>'                             # ячейка потоков
    r'(.*?)'                                                # group 2: содержимое потоков
    r'</td>',
    re.DOTALL,
)


def _parse_streams_from_html(streams_html: str) -> list[tuple[str, str, int | None]]:
    """Извлекает список рабочих потоков из HTML-фрагмента ячейки потоков.

    Returns:
        Список кортежей (url, format, bitrate), где bitrate может быть None.
    """
    stream_list: list[tuple[str, str, int | None]] = []
    for m in _PRINTLINK_RE.finditer(streams_html):
        url = m.group(1).strip()
        fmt = m.group(2).strip()
        style = m.group(4).strip()
        bitrate_str = m.group(5).strip().rstrip(",")

        # Пропускаем неработающие потоки
        if "не работает" in fmt.lower() or style == "link_inactive":
            continue

        # Пропускаем потоки без URL
        if not url or url == "#":
            continue

        # Определяем битрейт
        bitrate: int | None = None
        try:
            bitrate = int(bitrate_str) if bitrate_str else None
        except ValueError:
            bitrate = None

        # Очищаем формат от лишнего
        fmt_clean = fmt.replace(" (не работает)", "").strip()

        stream_list.append((url, fmt_clean, bitrate))
    return stream_list


def parse_station_page(html_text: str) -> dict:
    """Разбирает HTML страницы станции и возвращает структурированные данные.

    Returns:
        dict с ключами:
          - name: название станции (str | None)
          - cities: [(city_name, region | None), ...]
          - city_streams: {(city_name, region | None): [(url, format, bitrate), ...]}
          - streams: [(url, format, bitrate), ...] — все рабочие потоки
    """
    result: dict = {
        "name": None,
        "cities": [],
        "city_streams": {},
        "streams": [],
    }

    # 1) Извлечь название станции из <td class=city>...</td>
    m = re.search(r'<td\s+class=city>(.*?)</td>', html_text, re.DOTALL)
    if m:
        result["name"] = m.group(1).strip()

    # 2) Обрабатываем строки С регионом
    for m in _ROW_WITH_REGION_RE.finditer(html_text):
        city_name = m.group(1).strip()
        region = m.group(2).strip()
        streams_html = m.group(3)

        if city_name:
            result["cities"].append((city_name, region))
            city_stream_list = _parse_streams_from_html(streams_html)
            result["city_streams"][(city_name, region)] = city_stream_list
            result["streams"].extend(city_stream_list)

    # 3) Обрабатываем строки БЕЗ региона (Москва, Санкт-Петербург и т.п.)
    for m in _ROW_WITHOUT_REGION_RE.finditer(html_text):
        city_name = m.group(1).strip()
        streams_html = m.group(2)

        if city_name:
            # Пропускаем, если город уже был добавлен с регионом
            already_exists = any(c[0] == city_name for c in result["cities"])
            if not already_exists:
                result["cities"].append((city_name, None))
                city_stream_list = _parse_streams_from_html(streams_html)
                result["city_streams"][(city_name, None)] = city_stream_list
                result["streams"].extend(city_stream_list)

    return result


# ─── Выбор лучшего потока ────────────────────────────────────────────────────

def choose_best_stream(streams: list[tuple[str, str, int | None]]) -> str | None:
    """Выбирает лучший поток из списка по приоритету форматов и битрейту.

    Приоритет: mp3 > aac > m3u8 > ogg > opus.
    При одинаковых форматах — с большим битрейтом.
    """
    if not streams:
        return None

    def sort_key(item):
        url, fmt, bitrate = item
        fmt_lower = fmt.lower().split()[0] if fmt else ""
        priority = FORMAT_PRIORITY.get(fmt_lower, 99)
        return (priority, -(bitrate or 0))

    sorted_streams = sorted(streams, key=sort_key)
    return sorted_streams[0][0]


def choose_best_stream_moscow_priority(
    all_streams: list[tuple[str, str, int | None]],
    city_streams: dict[tuple, list[tuple]],
    cities: list[tuple[str, str | None]],
) -> str | None:
    """Выбирает streamUrl с приоритетом по Москве.

    Алгоритм:
      1. Если есть город «Москва» — берём лучший поток из Москвы
      2. Если Москвы нет — берём город, ближайший к Москве, с рабочими потоками
      3. Fallback — лучший из всех потоков
    """
    # 1) Попробовать Москву
    for city_name, region in cities:
        if city_name.lower().strip() == "москва":
            streams = city_streams.get((city_name, region), [])
            url = choose_best_stream(streams)
            if url:
                log.info("  🏙 Выбран поток Москвы")
                return url

    # 2) Найти ближайший к Москве город с рабочими потоками
    candidates: list[tuple[float, str, str | None, list]] = []
    for city_name, region in cities:
        streams = city_streams.get((city_name, region), [])
        if streams:
            dist = city_distance_from_moscow(city_name)
            candidates.append((dist, city_name, region, streams))

    if candidates:
        candidates.sort(key=lambda x: x[0])
        dist, city_name, region, streams = candidates[0]
        url = choose_best_stream(streams)
        if url:
            if dist < _UNKNOWN_CITY_DISTANCE:
                log.info("  📍 Выбран поток ближайшего к Москве города: %s (%.0f км)", city_name, dist)
            else:
                log.info("  📍 Выбран поток города: %s", city_name)
            return url

    # 3) Fallback — лучший из всех потоков
    return choose_best_stream(all_streams)


# ─── Скрапинг геральдики с ph4.ru ────────────────────────────────────────────

# Маппинг названий регионов из rosradio -> ph4.ru
# Названия могут отличаться (например, «Мордовия» vs «Республика Мордовия»)
_REGION_NAME_MAP: dict[str, str] = {
    "Адыгея": "Адыгея",
    "Алтайский край": "Алтайский край",
    "Амурская область": "Приамурье",
    "Архангельская область": "Архангельская область",
    "Астраханская область": "Астраханская область",
    "Башкортостан": "Башкортостан",
    "Белгородская область": "Белгородская область",
    "Брянская область": "Брянская область",
    "Бурятия": "Бурятия",
    "Владимирская область": "Владимирская область",
    "Волгоградская область": "Волгоградская область",
    "Вологодская область": "Вологодская область",
    "Воронежская область": "Воронежская область",
    "Дагестан": "Дагестан",
    "Еврейская автономная область": "Еврейская автономная область",
    "Забайкальский край": "Забайкалье",
    "Ивановская область": "Ивановская область",
    "Ингушетия": "Ингушетия",
    "Иркутская область": "Иркутская область",
    "Кабардино-Балкарская республика": "Кабардино-Балкария",
    "Калининградская область": "Калининградская область",
    "Калмыкия": "Калмыкия",
    "Калужская область": "Калужская область",
    "Камчатский край": "Камчатка",
    "Карачаево-Черкесская республика": "Карачаево-Черкесия",
    "Карелия": "Карелия",
    "Кемеровская область": "Кемеровская область",
    "Кировская область": "Кировская область",
    "Коми": "Коми",
    "Костромская область": "Костромская область",
    "Краснодарский край": "Краснодарский край",
    "Красноярский край": "Красноярский край",
    "Крым": "Крым",
    "Курганская область": "Курганская область",
    "Курская область": "Курская область",
    "Ленинградская область": "Ленинградская область",
    "Липецкая область": "Липецкая область",
    "Магаданская область": "Магаданская область",
    "Марий Эл": "Марий Эл",
    "Мордовия": "Мордовия",
    "Москва": "Московская область",
    "Московская область": "Московская область",
    "Мурманская область": "Мурманская область",
    "Ненецкий автономный округ": "Ненецкий автономный округ",
    "Нижегородская область": "Нижегородская область",
    "Новгородская область": "Новгородская область",
    "Новосибирская область": "Новосибирская область",
    "Омская область": "Омская область",
    "Оренбургская область": "Оренбургская область",
    "Орловская область": "Орловская область",
    "Пензенская область": "Пензенская область",
    "Пермский край": "Пермский край",
    "Приморский край": "Приморский край",
    "Псковская область": "Псковская область",
    "Республика Алтай": "Алтай",
    "Ростовская область": "Ростовская область",
    "Рязанская область": "Рязанская область",
    "Самарская область": "Самарская область",
    "Санкт-Петербург": "Ленинградская область",
    "Саратовская область": "Саратовская область",
    "Саха (Якутия)": "Республика Саха",
    "Сахалинская область": "Сахалинская область",
    "Свердловская область": "Свердловская область",
    "Северная Осетия-Алания": "Северная Осетия",
    "Смоленская область": "Смоленская область",
    "Ставропольский край": "Ставропольский край",
    "Тамбовская область": "Тамбовская область",
    "Татарстан": "Татарстан",
    "Тверская область": "Тверская область",
    "Томская область": "Томская область",
    "Тульская область": "Тульская область",
    "Тыва": "Тыва",
    "Тюменская область": "Тюменская область",
    "Удмуртская Республика": "Удмуртия",
    "Ульяновская область": "Ульяновская область",
    "Хабаровский край": "Хабаровский край",
    "Хакасия": "Хакасия",
    "Ханты-Мансийский автономный округ - Югра": "Ханты-Мансийский автономный округ",
    "Челябинская область": "Челябинская область",
    "Чеченская республика": "Чечня",
    "Чувашская Республика": "Чувашия",
    "Чукотский автономный округ": "Чукотка",
    "Ямало-Ненецкий автономный округ": "Ямало-Ненецкий автономный округ",
    "Ярославская область": "Ярославская область",
}

# Регулярки для парсинга страниц ph4.ru
_PH4_CITY_ARMS_RE = re.compile(
    r"<img src='(DL/HERALD/CITIES/ru/arms_[^']+)'[^>]*>.*?"
    r"<a href='(_dl\.php\?back=arm&a=\d+&b=[^&]+&d=arms)'[^>]*>Скачать",
    re.DOTALL,
)
_PH4_CITY_FLAG_RE = re.compile(
    r"<img src='(DL/HERALD/CITIES/ru/flags_[^']+)'[^>]*>.*?"
    r"<a href='(_dl\.php\?back=arm&a=\d+&b=[^&]+&d=flags)'[^>]*>Скачать",
    re.DOTALL,
)
_PH4_REGION_ARMS_RE = re.compile(
    r"<a href='(_dl\.php\?back=arm&a=\d+&b=[^&]+&d=arms)'[^>]*>"
    r"<img src='(DL/HERALD/COUNTRIES/ru/arms_[^']+)'[^>]*></a>",
    re.DOTALL,
)
_PH4_REGION_FLAG_RE = re.compile(
    r"<a href='(_dl\.php\?back=arm&a=\d+&b=[^&]+&d=flags)'[^>]*>"
    r"<img src='(DL/HERALD/COUNTRIES/ru/flags_[^']+)'[^>]*></a>",
    re.DOTALL,
)
_PH4_REGION_NAME_RE = re.compile(
    r"class=green>([^<]+)</a>",
)
_PH4_CITY_LIST_RE = re.compile(
    r"<a href='h_cities\.php\?d=(\d+)' class=map>([^<]+)</a>\s*"
    r"<span class=gray>-</span>\s*"
    r"<a href='h_index\.php\?y=12&int=l&ob=([^']+)' class=gray>([^<]+)</a>",
    re.DOTALL,
)
_PH4_REGION_LIST_RE = re.compile(
    r"<a href='h_countries\.php\?d=(\d+)' class=map>([^<]+)</a>",
)


def _fetch_ph4_page(url: str) -> str | None:
    """Скачивает страницу с ph4.ru и возвращает HTML или None."""
    try:
        resp = requests.get(url, headers=HEADERS, timeout=REQUEST_TIMEOUT)
        resp.raise_for_status()
        resp.encoding = "utf-8"
        return resp.text
    except requests.RequestException as e:
        log.warning("Ошибка при скачивании ph4.ru %s: %s", url, e)
        return None


def fetch_ph4_city_heraldry(city_ph4_id: int) -> dict | None:
    """Скачивает страницу города с ph4.ru и извлекает герб.

    Returns:
        dict с ключами: image_url, download_url, source
        или None, если герб не найден.
    """
    url = f"{PH4_BASE_URL}/h_cities.php?d={city_ph4_id}"
    html = _fetch_ph4_page(url)
    if not html:
        return None

    m = _PH4_CITY_ARMS_RE.search(html)
    if not m:
        log.debug("  Герб не найден на странице города ph4_id=%d", city_ph4_id)
        return None

    image_url = f"{PH4_BASE_URL}/{m.group(1)}"
    download_url = f"{PH4_BASE_URL}/{m.group(2)}"
    return {
        "image_url": image_url,
        "download_url": download_url,
        "source": url,
    }


def fetch_ph4_region_flag(region_ph4_id: int) -> dict | None:
    """Скачивает страницу региона с ph4.ru и извлекает флаг.

    Returns:
        dict с ключами: image_url, download_url, source, region_name
        или None, если флаг не найден.
    """
    url = f"{PH4_BASE_URL}/h_countries.php?d={region_ph4_id}"
    html = _fetch_ph4_page(url)
    if not html:
        return None

    m = _PH4_REGION_FLAG_RE.search(html)
    if not m:
        log.debug("  Флаг региона не найден на странице ph4_id=%d", region_ph4_id)
        return None

    image_url = f"{PH4_BASE_URL}/{m.group(2)}"
    download_url = f"{PH4_BASE_URL}/{m.group(1)}"

    # Извлечь полное название региона со страницы
    region_name = ""
    m_name = _PH4_REGION_NAME_RE.search(html)
    if m_name:
        region_name = m_name.group(1).strip()

    return {
        "image_url": image_url,
        "download_url": download_url,
        "source": url,
        "region_name": region_name,
    }


def fetch_ph4_cities_index() -> list[dict]:
    """Скачивает индексную страницу городов с ph4.ru и извлекает список.

    Returns:
        Список словарей: [{"id": int, "city": str, "region": str}, ...]
    """
    html = _fetch_ph4_page(PH4_CITIES_LIST_URL)
    if not html:
        return []

    cities = []
    for m in _PH4_CITY_LIST_RE.finditer(html):
        cities.append({
            "id": int(m.group(1)),
            "city": m.group(2).strip(),
            "region": m.group(4).strip(),
        })
    return cities


def fetch_ph4_regions_index() -> list[dict]:
    """Скачивает индексную страницу регионов с ph4.ru и извлекает список.

    Returns:
        Список словарей: [{"id": int, "region": str}, ...]
    """
    html = _fetch_ph4_page(PH4_REGIONS_LIST_URL)
    if not html:
        return []

    regions = []
    for m in _PH4_REGION_LIST_RE.finditer(html):
        regions.append({
            "id": int(m.group(1)),
            "region": m.group(2).strip(),
        })
    return regions


def _build_region_ph4_id_map() -> dict[str, int]:
    """Строит маппинг: название региона (rosradio) -> ph4_id.

    Скачивает индекс регионов с ph4.ru и сопоставляет названия
    с учётом _REGION_NAME_MAP.
    """
    ph4_regions = fetch_ph4_regions_index()
    # ph4_region_name -> ph4_id
    ph4_name_to_id: dict[str, int] = {r["region"]: r["id"] for r in ph4_regions}

    # rosradio_region_name -> ph4_id
    result: dict[str, int] = {}
    for rosradio_name, ph4_name in _REGION_NAME_MAP.items():
        ph4_id = ph4_name_to_id.get(ph4_name)
        if ph4_id is not None:
            result[rosradio_name] = ph4_id
        else:
            log.debug("  Регион '%s' (ph4: '%s') не найден на ph4.ru", rosradio_name, ph4_name)

    return result


def _build_city_ph4_id_map() -> dict[str, int]:
    """Строит маппинг: название города -> ph4_id.

    Скачивает индекс городов с ph4.ru.
    """
    ph4_cities = fetch_ph4_cities_index()
    # city_name -> ph4_id (для городов с уникальными названиями берём первый)
    city_to_id: dict[str, int] = {}
    for c in ph4_cities:
        # Если город уже есть — не перезаписываем (берём первое вхождение)
        if c["city"] not in city_to_id:
            city_to_id[c["city"]] = c["id"]
    return city_to_id


# ─── Основной скрапинг ─────────────────────────────────────────────────────

def fetch_station_list() -> list[tuple[str, str]]:
    """Скачивает страницу stations.htm и извлекает список станций."""
    log.info("Скачиваю список станций: %s", STATIONS_LIST_URL)
    resp = requests.get(STATIONS_LIST_URL, headers=HEADERS, timeout=REQUEST_TIMEOUT)
    resp.raise_for_status()
    resp.encoding = "utf-8"

    parser = StationListParser()
    parser.feed(resp.text)
    log.info("Найдено станций в списке: %d", len(parser.stations))
    return parser.stations


def fetch_station_page(href: str) -> dict | None:
    """Скачивает и парсит страницу отдельной станции."""
    url = urljoin(BASE_URL + "/", href)
    try:
        resp = requests.get(url, headers=HEADERS, timeout=REQUEST_TIMEOUT)
        resp.raise_for_status()
        resp.encoding = "utf-8"
    except requests.RequestException as e:
        log.warning("Ошибка при скачивании %s: %s", url, e)
        return None

    data = parse_station_page(resp.text)

    # Если название не распарсилось из HTML — пробуем из <title>
    if not data["name"]:
        m = re.search(r"<title>(.*?)</title>", resp.text, re.DOTALL | re.IGNORECASE)
        if m:
            title = m.group(1).strip()
            title = re.sub(r"\s*[–—]\s*Радио в России Online.*$", "", title).strip()
            data["name"] = title or None

    return data


def main():
    """Основная функция скрапера."""
    log.info("=== Старт скрапера rosradio-online.ru -> PostgreSQL ===")

    # 1) Подключение к БД и создание таблиц
    conn = get_connection()
    init_db(conn)

    # 2) Получить список станций
    station_list = fetch_station_list()

    total = len(station_list)
    success = 0
    skipped = 0

    for idx, (href, list_name) in enumerate(station_list, 1):
        log.info("[%d/%d] Обрабатываю: %s", idx, total, list_name)

        # 3) Скачать и распарсить страницу станции
        data = fetch_station_page(href)
        if data is None:
            skipped += 1
            time.sleep(REQUEST_DELAY)
            continue

        station_name = data["name"] or list_name
        cities = data["cities"]
        streams = data["streams"]
        city_streams = data.get("city_streams", {})

        # 4) Выбрать лучший поток (приоритет -- Москва, затем ближайший к Москве)
        stream_url = choose_best_stream_moscow_priority(streams, city_streams, cities)

        if not stream_url:
            log.warning("  Нет рабочих потоков для '%s' -- пропускаю", station_name)
            skipped += 1
            time.sleep(REQUEST_DELAY)
            continue

        # 5) Найти картинку станции через Yandex Images
        image_url = search_image_url(station_name)
        if not image_url:
            log.info("  Картинка не найдена (YT Music), пробую широкий поиск...")
            image_url = search_image_url_fallback(station_name)

        log.info("  Поток: %s  |  Городов: %d  |  Картинка: %s",
                 stream_url, len(cities), "да" if image_url else "нет")

        # 6) Вставить станцию
        station_id = upsert_station(
            conn, name=station_name, stream_url=stream_url, image_url=image_url
        )
        if station_id is None:
            log.warning("  Не удалось вставить станцию '%s'", station_name)
            skipped += 1
            time.sleep(REQUEST_DELAY)
            continue

        # 7) Вставить города и связи
        for city_name, region in cities:
            city_id = upsert_city(conn, name=city_name, region=region)
            if city_id:
                link_station_city(conn, station_id, city_id)

        conn.commit()
        success += 1

        time.sleep(REQUEST_DELAY)

    # 8) Обновить гербы городов и флаги регионов с ph4.ru
    log.info("=== Обновление геральдики городов и регионов с ph4.ru ===")
    _update_heraldry(conn)

    # 9) Итоги
    conn.close()
    log.info("=== Готово! Успешно: %d, Пропущено: %d, Всего: %d ===", success, skipped, total)


def update_images():
    """Обновляет картинки для станций, у которых их нет.

    Проходит по всем станциям с пустым imageurl и ищет картинку
    через Yandex Images (с фильтром music.youtube.com, затем fallback).
    """
    log.info("=== Обновление картинок станций ===")

    conn = get_connection()
    stations = get_stations_without_image(conn)
    log.info("Станций без картинки: %d", len(stations))

    updated = 0
    for station_id, station_name in stations:
        log.info("  Ищу картинку для '%s' (id=%d)...", station_name, station_id)

        image_url = search_image_url(station_name)
        if not image_url:
            log.info("  Не найдено (YT Music), пробую широкий поиск...")
            image_url = search_image_url_fallback(station_name)

        if image_url:
            update_station_image(conn, station_id, image_url)
            conn.commit()
            updated += 1
            log.info("  OK: %s", image_url[:80])
        else:
            log.warning("  Картинка не найдена для '%s'", station_name)

        time.sleep(REQUEST_DELAY)

    conn.close()
    log.info("=== Обновление картинок завершено. Обновлено: %d / %d ===", updated, len(stations))


def update_descriptions():
    """Обновляет описания для станций, у которых их нет.

    Проходит по всем станциям с пустым description и ищет описание
    через Wikipedia API.
    """
    log.info("=== Обновление описаний станций ===")

    conn = get_connection()
    stations = get_stations_without_description(conn)
    log.info("Станций без описания: %d", len(stations))

    updated = 0
    for station_id, station_name in stations:
        log.info("  Ищу описание для '%s' (id=%d)...", station_name, station_id)

        description = search_description(station_name)

        if description:
            update_station_description(conn, station_id, description)
            conn.commit()
            updated += 1
            log.info("  OK: %s", description[:80])
        else:
            log.warning("  Описание не найдено для '%s'", station_name)

        time.sleep(REQUEST_DELAY)

    conn.close()
    log.info("=== Обновление описаний завершено. Обновлено: %d / %d ===", updated, len(stations))


def _update_heraldry(conn):
    """Обновляет ссылки на гербы городов и флаги регионов с ph4.ru.

    Для каждого города без city_image_id — формирует ссылку на герб по ph4_id.
    Для каждого города без region_image_id — формирует ссылку на флаг региона по ph4_id.
    Фотографии НЕ скачиваются — в БД сохраняются только URL-ссылки.
    """
    # Строим маппинги названий -> ph4_id
    log.info("  Скачиваю индексы ph4.ru...")
    city_ph4_map = _build_city_ph4_id_map()
    region_ph4_map = _build_region_ph4_id_map()
    log.info("  Найдено городов на ph4.ru: %d, регионов: %d", len(city_ph4_map), len(region_ph4_map))

    # Кэш флагов регионов: region_name (rosradio) -> region_image_id
    region_flag_cache: dict[str, int] = {}

    # 1) Обновить ссылки на гербы городов
    cities_no_image = get_cities_without_images(conn)
    log.info("  Городов без герба: %d", len(cities_no_image))

    city_updated = 0
    for city_id, city_name, region in cities_no_image:
        ph4_city_id = city_ph4_map.get(city_name)
        if ph4_city_id is None:
            log.debug("    Город '%s' не найден на ph4.ru", city_name)
            continue

        # Формируем ссылки без скачивания страницы города
        source_url = f"{PH4_BASE_URL}/h_cities.php?d={ph4_city_id}"

        # Проверяем, не добавляли ли уже запись с этим source
        existing = get_city_image_by_source(conn, source_url)
        if existing is not None:
            update_city_image_ids(conn, city_id, city_image_id=existing, region_image_id=None)
            city_updated += 1
            continue

        # Скачиваем страницу города только чтобы узнать slug картинки
        heraldry = fetch_ph4_city_heraldry(ph4_city_id)
        if heraldry is None:
            continue

        city_image_id = insert_city_image(
            conn,
            image_url=heraldry["image_url"],
            download_url=heraldry["download_url"],
            source=source_url,
        )
        if city_image_id:
            update_city_image_ids(conn, city_id, city_image_id=city_image_id, region_image_id=None)
            city_updated += 1
            log.info("    🛡 Герб для '%s': %s", city_name, heraldry["image_url"])

        time.sleep(REQUEST_DELAY)

    conn.commit()
    log.info("  Гербы обновлены: %d / %d", city_updated, len(cities_no_image))

    # 2) Обновить ссылки на флаги регионов
    cities_no_region = get_cities_without_region_image(conn)
    log.info("  Городов без флага региона: %d", len(cities_no_region))

    region_updated = 0
    for city_id, city_name, region in cities_no_region:
        if region is None:
            continue

        # Проверяем кэш
        if region in region_flag_cache:
            update_city_image_ids(conn, city_id, city_image_id=None, region_image_id=region_flag_cache[region])
            region_updated += 1
            continue

        # Ищем ph4_id региона
        ph4_region_id = region_ph4_map.get(region)
        if ph4_region_id is None:
            log.debug("    Регион '%s' не найден на ph4.ru", region)
            continue

        # Формируем ссылки без скачивания страницы региона
        source_url = f"{PH4_BASE_URL}/h_countries.php?d={ph4_region_id}"

        # Проверяем, не добавляли ли уже запись с этим source
        existing = get_region_image_by_source(conn, source_url)
        if existing is not None:
            region_flag_cache[region] = existing
            update_city_image_ids(conn, city_id, city_image_id=None, region_image_id=existing)
            region_updated += 1
            continue

        # Скачиваем страницу региона только чтобы узнать slug картинки
        flag_data = fetch_ph4_region_flag(ph4_region_id)
        if flag_data is None:
            continue

        region_image_id = insert_region_image(
            conn,
            image_url=flag_data["image_url"],
            download_url=flag_data["download_url"],
            source=source_url,
        )
        if region_image_id:
            region_flag_cache[region] = region_image_id
            update_city_image_ids(conn, city_id, city_image_id=None, region_image_id=region_image_id)
            region_updated += 1
            log.info("    🚩 Флаг для региона '%s': %s", region, flag_data["image_url"])

        time.sleep(REQUEST_DELAY)

    conn.commit()
    log.info("  Флаги регионов обновлены: %d / %d", region_updated, len(cities_no_region))


def update_heraldry():
    """Отдельная команда: обновляет гербы городов и флаги регионов с ph4.ru."""
    log.info("=== Обновление геральдики с ph4.ru ===")

    conn = get_connection()
    init_db(conn)
    _update_heraldry(conn)
    conn.close()
    log.info("=== Обновление геральдики завершено ===")


if __name__ == "__main__":
    import sys
    if len(sys.argv) > 1 and sys.argv[1] == "--update-images":
        update_images()
    elif len(sys.argv) > 1 and sys.argv[1] == "--update-descriptions":
        update_descriptions()
    elif len(sys.argv) > 1 and sys.argv[1] == "--update-heraldry":
        update_heraldry()
    else:
        main()
