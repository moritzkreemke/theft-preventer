use serde::{Deserialize, Serialize};

#[derive(Debug, Serialize, Deserialize)]
pub struct Event {
    pub id: Option<i64>,
    pub state: String,
    pub lux: i32,
    pub temp: f64,
    pub phone: String,
    pub timestamp: i64,
}