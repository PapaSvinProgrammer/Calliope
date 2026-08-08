#!/usr/bin/env python3
"""Match radio station names to Yandex Object Storage image names.

The command is a dry run by default. Pass --apply to persist unambiguous matches.
"""

from __future__ import annotations

import argparse
import difflib
import re
import unicodedata
from dataclasses import dataclass
from pathlib import Path
from urllib.parse import quote

from db import get_connection

DEFAULT_IMAGE_DIR = Path("/Users/romanuraykin/Downloads/RUS_20200331")
DEFAULT_BUCKET_URL = "https://storage.yandexcloud.net/mordva-calliope"
IMAGE_EXTENSIONS = {".gif", ".jpeg", ".jpg", ".png", ".webp"}

TRANSLITERATION = str.maketrans(
    {
        "а": "a", "б": "b", "в": "v", "г": "g", "д": "d", "е": "e",
        "ё": "e", "ж": "zh", "з": "z", "и": "i", "й": "y", "к": "k",
        "л": "l", "м": "m", "н": "n", "о": "o", "п": "p", "р": "r",
        "с": "s", "т": "t", "у": "u", "ф": "f", "х": "h", "ц": "c",
        "ч": "ch", "ш": "sh", "щ": "sch", "ъ": "", "ы": "y", "ь": "",
        "э": "e", "ю": "yu", "я": "ya",
    }
)
REMOVABLE_WORDS = {"fm", "live", "moskau", "moskva", "radio"}

# Known transliteration and naming differences in this image set.
MANUAL_FILE_BY_STATION = {
    "Радио Балтик плюс": "Baltic_Plus.png",
    "Радио Бизнес-FM": "Business_FM_Moskau.png",
    "Радио Воскресение": "Kanal_Voskreseniye.png",
    "Радио Джаз": "Radio_Jazz.png",
    "Радио Москва-FM": "Moskva_FM.png",
    "Радио Ностальжи": "NOSTALGIE_101.5.png",
    "Радио Релакс-FM": "Relax_FM.png",
    "Радио Рок-FM": "Rock_FM_95.2.png",
    "Радио Серебряный дождь": "Radio_Silver_Rain.png",
    "Радио Слово": "Radio_Slovo.png",
    "Радио Столица": "Stolica_99.6.png",
    "Радио Страна-FM": "Strana_FM.png",
    "Радио Такси-FM": "Taxi_FM.png",
    "Радио Шоколад": "Radio_Chocolate.png",
    "Радио Эрмитаж": "Radio_Hermitage.png",
    "Радио Юмор-FM": "Humor_FM_.png",
}


@dataclass(frozen=True)
class Match:
    station_id: int
    station_name: str
    filename: str
    score: float
    reason: str


def variants(value: str) -> set[str]:
    """Return comparable variants with and without generic radio words."""
    normalized = unicodedata.normalize("NFKD", value.lower()).translate(TRANSLITERATION)
    tokens = re.findall(r"[a-z0-9]+", normalized)
    values = {
        "".join(tokens),
        "".join(token for token in tokens if token not in REMOVABLE_WORDS),
    }
    return {item for item in values if item}


def similarity(left: set[str], right: set[str]) -> float:
    return max(
        difflib.SequenceMatcher(None, left_item, right_item).ratio()
        for left_item in left
        for right_item in right
    )


def find_matches(
    stations: list[tuple[int, str]], files: list[Path], threshold: float, margin: float
) -> tuple[list[Match], list[tuple[int, str, str, float]]]:
    file_variants = {path.name: variants(path.stem) for path in files}
    file_names = set(file_variants)
    matches: list[Match] = []
    unmatched: list[tuple[int, str, str, float]] = []

    for station_id, station_name in stations:
        manual_filename = MANUAL_FILE_BY_STATION.get(station_name)
        if manual_filename and manual_filename in file_names:
            matches.append(Match(station_id, station_name, manual_filename, 1.0, "manual"))
            continue

        station_variants = variants(station_name)
        ranked = sorted(
            (
                (bool(station_variants & candidate_variants), similarity(station_variants, candidate_variants), filename)
                for filename, candidate_variants in file_variants.items()
            ),
            reverse=True,
        )
        exact, best_score, best_filename = ranked[0]
        second_score = ranked[1][1] if len(ranked) > 1 else 0.0

        if exact:
            matches.append(Match(station_id, station_name, best_filename, best_score, "exact"))
        elif best_score >= threshold and best_score - second_score >= margin:
            matches.append(Match(station_id, station_name, best_filename, best_score, "strong"))
        else:
            unmatched.append((station_id, station_name, best_filename, best_score))

    # A non-manual image should not be assigned to multiple differently named stations.
    by_filename: dict[str, list[Match]] = {}
    for match in matches:
        by_filename.setdefault(match.filename, []).append(match)

    safe: list[Match] = []
    for match in matches:
        collisions = by_filename[match.filename]
        if len(collisions) == 1 or match.reason == "manual":
            safe.append(match)
        else:
            names = ", ".join(item.station_name for item in collisions)
            unmatched.append((match.station_id, match.station_name, f"collision: {match.filename} -> {names}", match.score))

    return sorted(safe, key=lambda item: item.station_name), sorted(unmatched, key=lambda item: item[1])


def object_url(bucket_url: str, filename: str) -> str:
    return f"{bucket_url.rstrip('/')}/{quote(filename)}"


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--image-dir", type=Path, default=DEFAULT_IMAGE_DIR)
    parser.add_argument("--bucket-url", default=DEFAULT_BUCKET_URL)
    parser.add_argument("--threshold", type=float, default=0.86)
    parser.add_argument("--margin", type=float, default=0.08)
    parser.add_argument("--apply", action="store_true", help="Commit matched URLs to PostgreSQL")
    args = parser.parse_args()

    files = sorted(
        path for path in args.image_dir.iterdir()
        if path.is_file() and path.suffix.lower() in IMAGE_EXTENSIONS
    )
    if not files:
        parser.error(f"no supported images found in {args.image_dir}")

    conn = get_connection()
    try:
        with conn.cursor() as cursor:
            cursor.execute("SELECT id, name FROM radio_station ORDER BY name")
            stations = cursor.fetchall()

        matches, unmatched = find_matches(stations, files, args.threshold, args.margin)
        for match in matches:
            print(
                f"MATCH\t{match.reason}\t{match.score:.3f}\t{match.station_id}\t"
                f"{match.station_name}\t{match.filename}\t{object_url(args.bucket_url, match.filename)}"
            )

        print(f"\nSUMMARY files={len(files)} stations={len(stations)} safe_matches={len(matches)} unmatched={len(unmatched)}")
        if unmatched:
            print("\nREVIEW (not updated):")
            for station_id, station_name, candidate, score in unmatched:
                print(f"REVIEW\t{score:.3f}\t{station_id}\t{station_name}\t{candidate}")

        if not args.apply:
            print("\nDRY RUN: no database changes. Run again with --apply after reviewing matches.")
            conn.rollback()
            return 0

        with conn.cursor() as cursor:
            for match in matches:
                cursor.execute(
                    """
                    UPDATE radio_station
                    SET imageurl = %s, updatedat = NOW()
                    WHERE id = %s
                    """,
                    (object_url(args.bucket_url, match.filename), match.station_id),
                )
        conn.commit()
        print(f"\nUPDATED {len(matches)} radio_station rows")
        return 0
    except Exception:
        conn.rollback()
        raise
    finally:
        conn.close()


if __name__ == "__main__":
    raise SystemExit(main())
