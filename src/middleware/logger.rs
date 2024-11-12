use actix_web::dev::{ServiceRequest, ServiceResponse, Transform};
use actix_web::{Error, HttpMessage};
use futures::future::LocalBoxFuture;
use std::task::{Context, Poll};
use actix_web::body::MessageBody;
use actix_web::dev::Service;
use actix_web::web::Bytes;

pub struct Logger;

impl<S, B> Transform<S, ServiceRequest> for Logger
where
    S: Service<ServiceRequest, Response = ServiceResponse<B>, Error = Error> + 'static,
    B: MessageBody + 'static,
{
    type Response = ServiceResponse<B>;
    type Error = Error;
    type InitError = ();
    type Transform = LoggerMiddleware<S>;
    type Future = futures::future::Ready<Result<Self::Transform, Self::InitError>>;

    fn new_transform(&self, service: S) -> Self::Future {
        futures::future::ready(Ok(LoggerMiddleware { service }))
    }
}

pub struct LoggerMiddleware<S> {
    service: S,
}

impl<S, B> Service<ServiceRequest> for LoggerMiddleware<S>
where
    S: Service<ServiceRequest, Response = ServiceResponse<B>, Error = Error> + 'static,
    B: MessageBody + 'static,
{
    type Response = ServiceResponse<B>;
    type Error = Error;
    type Future = LocalBoxFuture<'static, Result<Self::Response, Self::Error>>;

    fn poll_ready(&self, cx: &mut Context<'_>) -> Poll<Result<(), Self::Error>> {
        self.service.poll_ready(cx)
    }

    fn call(&self, mut req: ServiceRequest) -> Self::Future {
        let method = req.method().clone();
        let path = req.path().to_owned();
        let query_string = req.query_string().to_owned();

        println!("Request: {} {}", method, path);
        if !query_string.is_empty() {
            println!("Query parameters: {}", query_string);
        }

        let body_future = req.extract::<Bytes>();
        let fut = self.service.call(req);

        Box::pin(async move {
            let body = body_future.await.unwrap_or_default();
            if !body.is_empty() {
                println!("Payload: {:?}", String::from_utf8_lossy(&body));
            }

            let res = fut.await?;
            Ok(res)
        })
    }
}