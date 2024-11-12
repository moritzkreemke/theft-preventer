use std::fmt;
use std::fmt::{Display, Formatter};
use actix_web::{HttpResponse, ResponseError};
use actix_web::http::StatusCode;
use serde_json::json;

#[derive(Debug)]
pub enum AppError {
    Unauthorized(String),
    InternalServerError(String),
    NotFound(String),
    // Add other error types as needed
}

impl fmt::Display for AppError {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        match self {
            AppError::Unauthorized(msg) => write!(f, "Unauthorized: {}", msg),
            AppError::InternalServerError(msg) => write!(f, "Internal Server Error: {}", msg),
            AppError::NotFound(msg) => write!(f, "Not Found: {}", msg),
            // Handle other error types
        }
    }
}

impl ResponseError for AppError {
    fn error_response(&self) -> HttpResponse {
        match self {
            AppError::Unauthorized(message) => HttpResponse::Unauthorized().json(json!({
                "error": "Unauthorized",
                "message": message
            })),
            AppError::InternalServerError(message) => HttpResponse::InternalServerError().json(json!({
                "error": "Internal Server Error",
                "message": message
            })),
            AppError::NotFound(message) => HttpResponse::NotFound().json(json!({
                "error": "Not Found",
                "message": message
            })),
            // Handle other error types
        }
    }

    fn status_code(&self) -> StatusCode {
        match self {
            AppError::Unauthorized(_) => StatusCode::UNAUTHORIZED,
            AppError::InternalServerError(_) => StatusCode::INTERNAL_SERVER_ERROR,
            AppError::NotFound(_) => StatusCode::NOT_FOUND,
            // Handle other error types
        }
    }
}