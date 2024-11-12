use actix_web::{web, HttpResponse};
use serde::{Deserialize, Serialize};
use serde_json::json;
use crate::config::Config;
use crate::db::{DbPool, Event, insert_event, get_latest_event, get_events};
use crate::error::AppError;
use crate::services;

#[derive(Deserialize, Serialize)]
pub struct EventInput {
    state: String,
    lux: i32,
    temp: f64,
    phone: String,
}

#[derive(Deserialize)]
pub struct LoginInfo {
    username: String,
    password: String,
}

#[derive(Deserialize)]
pub struct PaginationParams {
    page: Option<u32>,
    per_page: Option<u32>,
}

#[derive(Serialize)]
struct PaginatedResponse<T> {
    data: Vec<T>,
    page: u32,
    per_page: u32,
    total: u32,
}

pub async fn add_event(
    event_input: web::Json<EventInput>,
    pool: web::Data<DbPool>,
    config: web::Data<Config>
) -> Result<HttpResponse, AppError> {
    let event = Event {
        id: None,
        state: event_input.state.clone(),
        lux: event_input.lux,
        temp: event_input.temp,
        phone: event_input.phone.clone(),
        timestamp: chrono::Utc::now().timestamp(),
    };

    if event.phone == "disconnected" {
        if let Err(e) = services::twilio_service::send_sms(
            "Your door was open/closed while you were disconnected. Please check the app for more details.",
            &config
        ).await {
            eprintln!("Failed to send SMS: {}", e);
        }
    }

    match insert_event(&pool, &event) {
        Ok(_) => Ok(HttpResponse::Ok().json(event)),
        Err(_) => Err(AppError::InternalServerError("Failed to add event".to_string()))
    }
}

pub async fn login(
    info: web::Json<LoginInfo>,
    config: web::Data<Config>
) -> Result<HttpResponse, AppError> {
    if info.username == config.admin_username && info.password == config.admin_password {
        let token = crate::auth::create_jwt(&info.username, &config);
        Ok(HttpResponse::Ok().json(json!({"token": token})))
    } else {
        Err(AppError::Unauthorized("Invalid username or password".to_string()))
    }
}

pub async fn get_all_events(
    pool: web::Data<DbPool>,
    web::Query(params): web::Query<PaginationParams>,
) -> Result<HttpResponse, AppError> {
    let page = params.page.unwrap_or(1);
    let per_page = params.per_page.unwrap_or(10);

    match get_events(&pool, page, per_page) {
        Ok(events) => Ok(HttpResponse::Ok().json(events)),
        Err(_) => Err(AppError::InternalServerError("Failed to retrieve events".to_string()))
    }
}

pub async fn get_latest(pool: web::Data<DbPool>) -> Result<HttpResponse, AppError> {
    match get_latest_event(&pool) {
        Ok(Some(event)) => Ok(HttpResponse::Ok().json(event)),
        Ok(None) => Err(AppError::NotFound("No events found".to_string())),
        Err(_) => Err(AppError::InternalServerError("Failed to retrieve event".to_string()))
    }
}



pub fn config(cfg: &mut web::ServiceConfig) {
    cfg.service(
        web::scope("/events")
            .route("", web::get().to(get_all_events))
            .route("/latest", web::get().to(get_latest))
    )
        .service(
            web::scope("/add")
                .route("/event", web::post().to(add_event))
        )
        .route("/login", web::post().to(login));
}