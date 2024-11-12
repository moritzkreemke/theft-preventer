use std::env;
use serde::Deserialize;
use std::fs;
use std::io;
use std::path::PathBuf;

#[derive(Deserialize, Clone)]
pub struct Config {
    pub host: String,
    pub port: u16,
    pub secret_key: String,
    pub api_key: String,
    pub admin_username: String,
    pub admin_password: String,
    pub database_location: PathBuf,
}

impl Config {
    pub fn from_env_and_file() -> Result<Self, io::Error> {
        // Load from TOML file first
        let file_config = Self::from_file()?;

        // Override with environment variables where available
        Ok(Config {
            host: env::var("APP_HOST").unwrap_or(file_config.host),
            port: env::var("APP_PORT")
                .ok()
                .and_then(|s| s.parse().ok())
                .unwrap_or(file_config.port),
            secret_key: env::var("APP_SECRET_KEY").unwrap_or(file_config.secret_key),
            api_key: env::var("APP_API_KEY").unwrap_or(file_config.api_key),
            admin_username: env::var("APP_ADMIN_USERNAME").unwrap_or(file_config.admin_username),
            admin_password: env::var("APP_ADMIN_PASSWORD").unwrap_or(file_config.admin_password),
            database_location: env::var("APP_DATABASE_LOCATION")
                .map(PathBuf::from)
                .unwrap_or(file_config.database_location),
        })
    }

    fn from_file() -> Result<Self, io::Error> {
        let config_str = fs::read_to_string("config.toml")?;
        toml::from_str(&config_str).map_err(|e| io::Error::new(io::ErrorKind::InvalidData, e))
    }
}

pub fn load_config() -> Result<Config, io::Error> {
    Config::from_env_and_file()
}