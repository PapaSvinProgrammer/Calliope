#!/usr/bin/env python3
"""Убивает все зависшие/зависающие сессии в БД radio_db.

Запуск:
    source venv/bin/activate
    python3 kill_stuck_sessions.py

Используйте, если scraper.py завис: orphaned-транзакция держит
блокировку и init_db висит на ShareLock.
"""
from db import get_connection


def main():
    conn = get_connection()
    conn.autocommit = True
    try:
        with conn.cursor() as cur:
            cur.execute(
                """
                SELECT pid, state, wait_event_type, wait_event,
                       query_start, left(query, 100)
                FROM pg_stat_activity
                WHERE datname = %s AND pid != pg_backend_pid()
                ORDER BY query_start
                """,
                ("radio_db",),
            )
            rows = cur.fetchall()
            if not rows:
                print("Нет активных сессий на radio_db — всё чисто.")
                return

            print(f"Найдено сессий: {len(rows)}")
            for r in rows:
                print(f"  pid={r[0]} state={r[1]} wait={r[2]}/{r[3]} "
                      f"started={r[4]} query={r[5]!r}")

            print("\nУбиваю все сессии...")
            cur.execute(
                """
                SELECT pg_terminate_backend(pid), pid, state
                FROM pg_stat_activity
                WHERE datname = %s AND pid != pg_backend_pid()
                """,
                ("radio_db",),
            )
            killed = cur.fetchall()
            for r in killed:
                print(f"  terminated pid={r[1]} state={r[2]} -> {r[0]}")
            print(f"\nГотово. Убито сессий: {len(killed)}")
    finally:
        conn.close()


if __name__ == "__main__":
    main()
