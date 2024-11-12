mod models;

use r2d2::Pool;
use r2d2_sqlite::SqliteConnectionManager;
use rusqlite::Result;
use std::path::Path;

pub use models::Event;

pub type DbPool = Pool<SqliteConnectionManager>;

pub fn init_pool<P: AsRef<Path>>(path: P) -> Result<DbPool> {
    let manager = SqliteConnectionManager::file(path);
    let pool = Pool::new(manager).expect("Failed to create pool");

    // Initialize the database
    let conn = pool.get().expect("Failed to get connection from pool");
    conn.execute(
        "CREATE TABLE IF NOT EXISTS events (
            id INTEGER PRIMARY KEY,
            state TEXT NOT NULL,
            lux INTEGER NOT NULL,
            temp REAL NOT NULL,
            phone TEXT NOT NULL,
            timestamp INTEGER NOT NULL
        )",
        [],
    )?;

    Ok(pool)
}

pub fn insert_event(pool: &DbPool, event: &Event) -> Result<i64> {
    let conn = pool.get().expect("Failed to get connection from pool");
    conn.execute(
        "INSERT INTO events (state, lux, temp, phone, timestamp) VALUES (?1, ?2, ?3, ?4, ?5)",
        (
            &event.state,
            event.lux,
            event.temp,
            &event.phone,
            event.timestamp,
        ),
    )?;
    Ok(conn.last_insert_rowid())
}

pub fn get_latest_event(pool: &DbPool) -> Result<Option<Event>> {
    let conn = pool.get().expect("Failed to get connection from pool");
    let mut stmt = conn.prepare(
        "SELECT id, state, lux, temp, phone, timestamp FROM events ORDER BY timestamp DESC LIMIT 1",
    )?;
    let mut event_iter = stmt.query_map([], |row| {
        Ok(Event {
            id: Some(row.get(0)?),
            state: row.get(1)?,
            lux: row.get(2)?,
            temp: row.get(3)?,
            phone: row.get(4)?,
            timestamp: row.get(5)?,
        })
    })?;

    event_iter.next().transpose()
}

pub fn get_events(pool: &DbPool, page: u32, per_page: u32) -> Result<Vec<Event>> {
    let conn = pool.get().expect("Failed to get connection from pool");
    let offset = (page - 1) * per_page;
    let mut stmt = conn.prepare(
        "SELECT id, state, lux, temp, phone, timestamp
         FROM events
         ORDER BY timestamp DESC
         LIMIT ?1 OFFSET ?2"
    )?;
    let event_iter = stmt.query_map([per_page, offset], |row| {
        Ok(Event {
            id: Some(row.get(0)?),
            state: row.get(1)?,
            lux: row.get(2)?,
            temp: row.get(3)?,
            phone: row.get(4)?,
            timestamp: row.get(5)?,
        })
    })?;

    event_iter.collect()
}

pub fn get_total_events(pool: &DbPool) -> Result<u32> {
    let conn = pool.get().expect("Failed to get connection from pool");
    conn.query_row("SELECT COUNT(*) FROM events", [], |row| row.get(0))
}