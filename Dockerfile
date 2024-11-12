#backend builder
FROM rust:1.82 as backend

WORKDIR /usr/src/app
COPY Cargo.toml Cargo.lock ./
COPY src ./src

RUN cargo build --release

FROM node:22 as frontend
WORKDIR /usr/src/app
COPY frontend/package.json frontend/package-lock.json ./
RUN npm install

COPY frontend/ ./
RUN npm run build

FROM rust:1.82

WORKDIR /app
COPY --from=backend /usr/src/app/target/release/theft-preventer /app
COPY --from=frontend /usr/src/app/dist /app/frontend/dist
COPY config.toml /app

RUN groupadd -r appuser && useradd -r -g appuser appuser
RUN mkdir -p /app/data && chown -R appuser:appuser /app && chmod 755 /app/data
USER appuser

EXPOSE 8080

CMD ["/app/theft-preventer"]