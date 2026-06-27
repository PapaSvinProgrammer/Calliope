#!/usr/bin/env python3
"""
Test script for searching image URLs via Google Images and Yandex Images.
Testing the approach manually before integrating into scraper.py.
"""

import re
import json
import time

import requests

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

# Precompiled regex patterns
_RE_OU = re.compile(r'"ou"\s*:\s*"([^"]+)"')
_RE_IMG_IN_SCRIPT = re.compile(r'"(https?://[^"]+\.(?:jpg|jpeg|png|webp|gif|svg)[^"]*)"')
_RE_DATA_BEM = re.compile(r'data-bem="([^"]+)"')
_RE_IMG_HREF = re.compile(r'"img_href"\s*:\s*"([^"]+)"')
_RE_IMG_SRC = re.compile(r'<img[^>]+src="(https?://[^"]+)"[^>]*>')


# --- Google Images -----------------------------------------------------------

def search_google_images(query, num_results=5):
    """Search images via Google Images by scraping HTML result.

    Returns:
        List of direct image URLs.
    """
    url = "https://www.google.com/search"
    params = {
        "q": query,
        "tbm": "isch",
        "hl": "ru",
        "gl": "ru",
        "num": num_results,
    }

    try:
        resp = requests.get(
            url, params=params, headers=HEADERS, timeout=REQUEST_TIMEOUT
        )
        resp.raise_for_status()
    except requests.RequestException as e:
        print("  Google Images error: {}".format(e))
        return []

    image_urls = []

    # Method 1: Extract "ou":"https://..." (original image URL)
    for m in _RE_OU.finditer(resp.text):
        img_url = m.group(1)
        if img_url.startswith("http") and img_url not in image_urls:
            image_urls.append(img_url)

    # Method 2: If method 1 failed, look for image URLs in scripts
    if not image_urls:
        for m in _RE_IMG_IN_SCRIPT.finditer(resp.text):
            img_url = m.group(1)
            if any(skip in img_url.lower() for skip in ["google", "gstatic", "favicon"]):
                continue
            if img_url not in image_urls:
                image_urls.append(img_url)

    return image_urls[:num_results]


# --- Yandex Images -----------------------------------------------------------

def search_yandex_images(query, num_results=5):
    """Search images via Yandex Images by scraping HTML result.

    Returns:
        List of direct image URLs.
    """
    url = "https://yandex.ru/images/search"
    params = {
        "text": query,
        "nl": "1",
    }

    try:
        resp = requests.get(
            url, params=params, headers=HEADERS, timeout=REQUEST_TIMEOUT
        )
        resp.raise_for_status()
    except requests.RequestException as e:
        print("  Yandex Images error: {}".format(e))
        return []

    image_urls = []

    # Method 1: Yandex embeds data in data-bem attributes with JSON
    for m in _RE_DATA_BEM.finditer(resp.text):
        try:
            json_str = (
                m.group(1)
                .replace(""", '"')
                .replace("&", "&")
                .replace("<", "<")
                .replace(">", ">")
            )
            data = json.loads(json_str)
            serp_item = data.get("serp-item", data)
            if isinstance(serp_item, dict):
                img_url = serp_item.get("img_href", "")
                if img_url and img_url.startswith("http") and img_url not in image_urls:
                    image_urls.append(img_url)
        except (json.JSONDecodeError, KeyError, TypeError):
            continue

    # Method 2: Look for img_href in JSON-like structures
    if not image_urls:
        for m in _RE_IMG_HREF.finditer(resp.text):
            img_url = m.group(1)
            if img_url.startswith("http") and img_url not in image_urls:
                image_urls.append(img_url)

    # Method 3: Look for direct image URLs in src attributes
    if not image_urls:
        for m in _RE_IMG_SRC.finditer(resp.text):
            img_url = m.group(1)
            if any(skip in img_url.lower() for skip in ["yastatic", "favicon", "avatar", "badge"]):
                continue
            if img_url not in image_urls:
                image_urls.append(img_url)

    return image_urls[:num_results]


# --- Combined search ---------------------------------------------------------

def search_image_url(query, prefer="yandex"):
    """Search image URL using Google and Yandex Images.

    Args:
        query: Search query (e.g. "like fm radio logo")
        prefer: "yandex" or "google" -- which source to try first

    Returns:
        URL of the first found image, or None.
    """
    if prefer == "yandex":
        sources = [
            ("Yandex", search_yandex_images),
            ("Google", search_google_images),
        ]
    else:
        sources = [
            ("Google", search_google_images),
            ("Yandex", search_yandex_images),
        ]

    for source_name, search_func in sources:
        print("\n  Searching via {} Images: '{}'".format(source_name, query))
        urls = search_func(query, num_results=5)
        if urls:
            print("  {}: found {} images".format(source_name, len(urls)))
            for i, u in enumerate(urls, 1):
                print("    {}. {}".format(i, u))
            return urls[0]
        else:
            print("  {}: nothing found".format(source_name))

    return None


# --- Test --------------------------------------------------------------------

if __name__ == "__main__":
    test_queries = [
        "like fm radio",
        "russkoe radio logo",
        "evropa plus logo",
    ]

    for q in test_queries:
        print("\n" + "=" * 60)
        print("Query: '{}'".format(q))
        print("=" * 60)

        result = search_image_url(q, prefer="yandex")

        if result:
            print("\n  Result: {}".format(result))
        else:
            print("\n  No image found")

        time.sleep(2)
