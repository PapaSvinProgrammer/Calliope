#!/usr/bin/env python3
"""
Поиск картинок радиостанций через Yandex Images.

Использует фильтр site=music.youtube.com для получения обложек
станций с YouTube Music (высокое качество, актуальные логотипы).

Yandex Images отдаёт HTML с JSON-данными, где URL картинок лежат
в поле img_href (HTML-encoded). После html.unescape() парсим regex-ом.
"""

import re
import html as htmlmod

import requests

# Регулярка для извлечения img_href из Yandex Images HTML
_RE_IMG_HREF = re.compile(r'"img_href"\s*:\s*"([^"]+)"')

# User-Agent и заголовки
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

REQUEST_TIMEOUT = 15


def search_image_url(station_name: str, site_filter: str = "music.youtube.com") -> str:
    """Ищет URL картинки станции через Yandex Images.

    По умолчанию ищет с фильтром site=music.youtube.com -- обложки станций
    с YouTube Music. Запрос: "{station_name} radio".

    Args:
        station_name: Название радиостанции (например, "Like FM").
        site_filter: Домен-фильтр для Yandex Images (по умолчанию music.youtube.com).

    Returns:
        URL картинки или пустую строку.
    """
    query = f"{station_name} radio"
    url = "https://yandex.ru/images/search"
    params = {
        "text": query,
        "nl": "1",
    }
    if site_filter:
        params["site"] = site_filter

    try:
        resp = requests.get(url, params=params, headers=HEADERS, timeout=REQUEST_TIMEOUT)
        resp.raise_for_status()
    except requests.RequestException:
        return ""

    decoded = htmlmod.unescape(resp.text)

    for m in _RE_IMG_HREF.finditer(decoded):
        img_url = m.group(1)
        if img_url.startswith("http"):
            return img_url

    return ""


def search_image_url_fallback(station_name: str) -> str:
    """Ищет URL картинки без фильтра по домену (широкий поиск).

    Используется как fallback, если поиск с site=radio.yandex.ru
    не дал результатов.

    Args:
        station_name: Название радиостанции.

    Returns:
        URL картинки или пустую строку.
    """
    return search_image_url(station_name, site_filter="")
