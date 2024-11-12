use actix_files as fs;
use actix_web::{web, App, HttpServer, dev::ServiceRequest, Error};
use actix_web::dev::ServiceResponse;
use actix_web::web::Data;
use crate::config::Config;
use actix_web_httpauth::middleware::HttpAuthentication;
use actix_web_httpauth::extractors::bearer::BearerAuth;
use futures::future::{ok, err, Ready};

mod api;
mod config;
mod middleware;
mod db;
mod auth;
mod error;

#[actix_web::main]
async fn main() -> std::io::Result<()> {
    let config = config::load_config().map_err(|e| {
        eprintln!("Failed to load config: {}", e);
        std::io::Error::new(std::io::ErrorKind::Other, "Config loading failed")
    })?;
    let config = web::Data::new(config);

    let host = config.host.clone();
    let port = config.port;

    println!("Starting server at {}:{}", host, port);

    let db_location = config.database_location.clone();
    let pool = db::init_pool(db_location).expect("Failed to create pool");
    let pool = web::Data::new(pool);

    HttpServer::new(move || {
        let config = config.clone();
        let auth = HttpAuthentication::bearer(move |req: ServiceRequest, credentials: BearerAuth| {
            let config = req.app_data::<web::Data<Config>>().unwrap();
            let fut: Ready<Result<ServiceRequest, (Error, ServiceRequest)>> =
                if auth::jwt_validator(credentials.token(), config.get_ref()).is_ok() {
                    ok(req)
                } else {
                    err((actix_web::error::ErrorUnauthorized("Invalid token"), req))
                };
            fut
        });

        let config = config.clone();
        let api_key_auth = HttpAuthentication::with_fn(move |req: ServiceRequest, _: Option<BearerAuth>| {
            let config = req.app_data::<web::Data<Config>>().unwrap();
            let fut: Ready<Result<ServiceRequest, (Error, ServiceRequest)>> =
                if let Some(api_key) = req.headers().get("X-API-Key") {
                    if auth::api_key_validator(api_key.to_str().unwrap_or(""), config.get_ref()).is_ok() {
                        ok(req)
                    } else {
                        err((actix_web::error::ErrorUnauthorized("Invalid API key"), req))
                    }
                } else {
                    err((actix_web::error::ErrorUnauthorized("Missing API key"), req))
                };
            fut
        });

        App::new()
            .app_data(config.clone())
            .app_data(pool.clone())
            .service(
                web::scope("/api")
                    .service(
                        web::scope("/events")
                            .wrap(auth.clone())
                            .route("", web::get().to(api::routes::get_all_events))
                            .route("/latest", web::get().to(api::routes::get_latest))
                    )
                    .service(
                        web::scope("/add")
                            .wrap(api_key_auth.clone())
                            .route("/event", web::post().to(api::routes::add_event))
                    )
                    .service(web::resource("/login").route(web::post().to(api::routes::login)))
            )
            .service(
                fs::Files::new("/", "./frontend/dist")
                    .index_file("index.html")
                    .default_handler(|req: ServiceRequest| {
                        let (http_req, _) = req.into_parts();
                        async {
                            let response = fs::NamedFile::open("./frontend/dist/index.html")?.into_response(&http_req);
                            Ok(ServiceResponse::new(http_req, response))
                        }
                    })
            )
    })
        .bind((host, port))?
        .run()
        .await
}