# -- BUILDER --
FROM node:24-bookworm AS builder

RUN apt-get update && apt-get install -y \
  curl \
  git \
  build-essential \
  binaryen \
  pkg-config \
  libssl-dev \
  ca-certificates \
  && rm -rf /var/lib/apt/lists/*

# Setup Rust
ENV RUSTUP_HOME="/usr/local/rustup" \
    CARGO_HOME="/usr/local/cargo" \
    PATH="/usr/local/cargo/bin:${PATH}"

RUN curl https://sh.rustup.rs -sSf | sh -s -- -y \
    --profile minimal \
    --default-toolchain 1.94.0

RUN rustup target add wasm32-unknown-unknown \
 && cargo install cargo-run-bin --version 1.7.5 \
 && cargo install wasm-bindgen-cli --version 0.2.100

RUN corepack enable

WORKDIR /app

COPY noclip.website/pnpm-lock.yaml ./
RUN pnpm fetch

COPY noclip.website/package.json ./
RUN pnpm install --offline --frozen-lockfile

COPY noclip.website/ .

RUN cd rust && cargo build --locked --target wasm32-unknown-unknown
RUN pnpm run build

## -- Runtime image --
FROM nginx:alpine

COPY --from=builder /app/dist /usr/share/nginx/html
