#!/usr/bin/env python3
"""
Поиск описаний радиостанций через Wikipedia API.

Использует MediaWiki API (ru.wikipedia.org) для поиска статьи о станции
и извлечения краткого описания (extract) из введения статьи.

Алгоритм:
  1. Поиск статьи по запросу "{station_name} радио"
  2. Проверка: заголовок найденной статьи должен содержать название станции
  3. Если не совпадает -- повторный поиск с уточнённым запросом
  4. Получение extract (текст введения) через action=query&prop=extracts
  5. Возврат первого абзаца как описания
"""

import re

import requests

WIKI_API_URL = "https://ru.wikipedia.org/w/api.php"

HEADERS = {
    "User-Agent": "RadioScraper/1.0 (https://github.com/example; test@example.com)",
}

REQUEST_TIMEOUT = 15


def _normalize(name: str) -> str:
    """Нормализует название для сравнения: нижний регистр, без лишних символов.

    Также заменяет латинское 'radio' на кириллическое 'радио',
    чтобы 'Comedy Radio' и 'Comedy радио' считались одинаковыми.
    """
    text = name.lower().strip()
    # Нормализуем латинское "radio" -> кириллическое "радио"
    text = re.sub(r"\bradio\b", "радио", text)
    return re.sub(r"[^a-zа-яё0-9]", "", text)


def _title_matches_station(title: str, station_name: str) -> bool:
    """Проверяет, что заголовок статьи соответствует названию станции.

    Статья считается релевантной, если нормализованное название станции
    содержится в нормализованном заголовке статьи, ИЛИ наоборот --
    нормализованный заголовок содержится в названии станции.
    Это нужно для случаев типа: станция "Радио DFM", статья "DFM".
    """
    norm_title = _normalize(title)
    norm_station = _normalize(station_name)
    return norm_station in norm_title or norm_title in norm_station


def _extract_is_about_radio(extract: str) -> bool:
    """Проверяет, что описание действительно о радиостанции.

    Ищет ключевые слова: радиостанция, радио, вещание, эфир, частота.
    """
    keywords = ["радиостанция", "радио", "вещание", "эфир", "частота"]
    text_lower = extract.lower()
    return any(kw in text_lower for kw in keywords)


def _wiki_search(query: str, srlimit: int = 5) -> list[dict]:
    """Ищет статьи в Wikipedia по запросу.

    Returns:
        Список словарей с ключами: pageid, title.
    """
    params = {
        "action": "query",
        "list": "search",
        "srsearch": query,
        "format": "json",
        "srlimit": srlimit,
    }

    try:
        resp = requests.get(
            WIKI_API_URL, params=params, headers=HEADERS, timeout=REQUEST_TIMEOUT
        )
        resp.raise_for_status()
        data = resp.json()
    except (requests.RequestException, ValueError):
        return []

    return [
        {"pageid": item["pageid"], "title": item["title"]}
        for item in data.get("query", {}).get("search", [])
    ]


def _get_extract(pageid: int) -> str:
    """Получает текст введения статьи по pageid.

    Returns:
        Первый непустой абзац или пустая строка.
    """
    params = {
        "action": "query",
        "pageids": pageid,
        "prop": "extracts",
        "exintro": True,
        "explaintext": True,
        "format": "json",
    }

    try:
        resp = requests.get(
            WIKI_API_URL, params=params, headers=HEADERS, timeout=REQUEST_TIMEOUT
        )
        resp.raise_for_status()
        data = resp.json()
    except (requests.RequestException, ValueError):
        return ""

    page_data = data.get("query", {}).get("pages", {}).get(str(pageid), {})
    extract = page_data.get("extract", "")

    if not extract:
        return ""

    paragraphs = [p.strip() for p in extract.split("\n") if p.strip()]
    if paragraphs:
        return paragraphs[0]

    return ""


def _strip_radio_prefix(name: str) -> str:
    """Убирает префикс 'Радио' из названия для лучшего поиска.

    Например: 'Радио Восток-FM' -> 'Восток-FM'
               'Радио DFM' -> 'DFM'
               'Русское радио' -> 'Русское радио' (не убирается, т.к. 'радио' в конце)
    """
    # Убираем "Радио " в начале (с пробелом и дефисом)
    stripped = re.sub(r"^Радио\s+", "", name, flags=re.IGNORECASE)
    stripped = re.sub(r"^Радио-", "", stripped, flags=re.IGNORECASE)
    if stripped != name:
        return stripped
    return name


def _make_english_variants(name: str) -> list[str]:
    """Создаёт варианты названия с заменой 'радио' на 'Radio'.

    Например: 'Comedy радио' -> ['Comedy Radio', 'Comedy Radio радио']
               'Радио DFM' -> ['DFM Radio', 'DFM Radio радио']

    Returns:
        Список альтернативных названий для поиска.
    """
    variants = []

    # Заменяем "радио" на "Radio" (в конце или отдельно)
    replaced = re.sub(r"\bрадио\b", "Radio", name, flags=re.IGNORECASE)
    if replaced != name:
        variants.append(replaced)

    # Убираем префикс "Радио" и добавляем "Radio" в конец
    clean = _strip_radio_prefix(name)
    if clean != name:
        # Если "радио" не осталось в названии, добавляем "Radio"
        if "radio" not in clean.lower() and "радио" not in clean.lower():
            variants.append(f"{clean} Radio")

    return variants


def search_description(station_name: str) -> str:
    """Ищет описание радиостанции через Wikipedia API.

    Алгоритм:
      1. Убирает префикс "Радио" из названия (для лучшего поиска)
      2. Поиск по запросу "{clean_name} радио"
      3. Проверка: заголовок статьи должен соответствовать станции
      4. Проверка: описание должно быть о радиостанции
      5. Если не найдено -- повторный поиск "{clean_name} радиостанция"
      6. Если не найдено -- поиск по оригинальному названию

    Args:
        station_name: Название радиостанции (например, "Радио DFM", "Русское радио").

    Returns:
        Текст описания (первый абзац из Wikipedia) или пустая строка.
    """
    clean_name = _strip_radio_prefix(station_name)

    # Попытка 1: поиск "{clean_name} радио"
    results = _wiki_search(f"{clean_name} радио", srlimit=5)
    for result in results:
        if _title_matches_station(result["title"], station_name):
            extract = _get_extract(result["pageid"])
            if extract and _extract_is_about_radio(extract):
                return extract

    # Попытка 2: поиск "{clean_name} радиостанция"
    results = _wiki_search(f"{clean_name} радиостанция", srlimit=5)
    for result in results:
        if _title_matches_station(result["title"], station_name):
            extract = _get_extract(result["pageid"])
            if extract and _extract_is_about_radio(extract):
                return extract

    # Попытка 3: поиск по чистому названию (без "радио")
    results = _wiki_search(clean_name, srlimit=5)
    for result in results:
        if _title_matches_station(result["title"], station_name):
            extract = _get_extract(result["pageid"])
            if extract and _extract_is_about_radio(extract):
                return extract

    # Попытка 4: поиск по оригинальному названию (с "Радио")
    if clean_name != station_name:
        results = _wiki_search(f"{station_name}", srlimit=5)
        for result in results:
            if _title_matches_station(result["title"], station_name):
                extract = _get_extract(result["pageid"])
                if extract and _extract_is_about_radio(extract):
                    return extract

    # Попытка 5: поиск с английским вариантом "Radio"
    english_variants = _make_english_variants(station_name)
    for variant in english_variants:
        results = _wiki_search(f"{variant} радио", srlimit=5)
        for result in results:
            if _title_matches_station(result["title"], station_name):
                extract = _get_extract(result["pageid"])
                if extract and _extract_is_about_radio(extract):
                    return extract

    # Не найдено релевантной статьи
    return ""
