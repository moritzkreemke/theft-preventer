use actix_web::dev::ServiceRequest;
use actix_web::{Error, HttpMessage};
use actix_web::error::ErrorUnauthorized;
use actix_web_httpauth::extractors::bearer::BearerAuth;
use jsonwebtoken::{decode, encode, DecodingKey, EncodingKey, Header, Validation};
use serde::{Deserialize, Serialize};
use crate::config::Config;
use crate::error::AppError;

#[derive(Debug, Serialize, Deserialize)]
struct Claims {
    sub: String,
    exp: usize,
}

pub fn create_jwt(username: &str, config: &Config) -> String {
    let expiration = chrono::Utc::now()
        .checked_add_signed(chrono::Duration::hours(1))
        .expect("valid timestamp")
        .timestamp();

    let claims = Claims {
        sub: username.to_owned(),
        exp: expiration as usize,
    };

    encode(&Header::default(), &claims, &EncodingKey::from_secret(config.secret_key.as_ref()))
        .unwrap()
}

pub fn jwt_validator(token: &str, config: &Config) -> Result<(), AppError> {
    if verify_jwt(token, config) {
        Ok(())
    } else {
        Err(AppError::Unauthorized("Invalid token".to_string()))
    }
}

pub fn api_key_validator(api_key: &str, config: &Config) -> Result<(), AppError> {
    if api_key == config.api_key {
        Ok(())
    } else {
        Err(AppError::Unauthorized("Invalid API key".to_string()))
    }
}
fn verify_jwt(token: &str, config: &Config) -> bool {
    decode::<Claims>(
        token,
        &DecodingKey::from_secret(config.secret_key.as_ref()),
        &Validation::default(),
    )
        .is_ok()
}