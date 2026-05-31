#!/usr/bin/env bash

set -euo pipefail

BASE_DIR="$(cd "$(dirname "$0")" && pwd)"
TLS_DIR="${1:-$BASE_DIR/../src/main/resources/tls}"
TMP_DIR="$(mktemp -d)"
PASSWORD="changeit"

run_keytool() {
  if command -v keytool >/dev/null 2>&1; then
    keytool "$@"
    return
  fi

  if command -v docker >/dev/null 2>&1; then
    docker run --rm -v "$TLS_DIR:/tls" eclipse-temurin:21-jdk keytool "$@"
    return
  fi

  echo "keytool or docker is required to create truststores" >&2
  exit 1
}

cleanup() {
  rm -rf "$TMP_DIR"
}

trap cleanup EXIT

mkdir -p "$TLS_DIR"
rm -f "$TLS_DIR"/*.p12 "$TLS_DIR"/*.crt

cat >"$TMP_DIR/server-ext.cnf" <<'EOF'
basicConstraints=CA:FALSE
keyUsage=digitalSignature,keyEncipherment
extendedKeyUsage=serverAuth
subjectAltName=DNS:localhost,IP:127.0.0.1,DNS:lab3-server
EOF

cat >"$TMP_DIR/client-ext.cnf" <<'EOF'
basicConstraints=CA:FALSE
keyUsage=digitalSignature,keyEncipherment
extendedKeyUsage=clientAuth
EOF

openssl req -x509 -newkey rsa:2048 -nodes \
  -keyout "$TMP_DIR/ca.key" \
  -out "$TLS_DIR/ca.crt" \
  -days 3650 \
  -subj "/C=RU/ST=Moscow/L=Moscow/O=Course/OU=SABD/CN=lab3-ca"

openssl req -newkey rsa:2048 -nodes \
  -keyout "$TMP_DIR/server.key" \
  -out "$TMP_DIR/server.csr" \
  -subj "/C=RU/ST=Moscow/L=Moscow/O=Course/OU=SABD/CN=localhost"

openssl x509 -req \
  -in "$TMP_DIR/server.csr" \
  -CA "$TLS_DIR/ca.crt" \
  -CAkey "$TMP_DIR/ca.key" \
  -CAcreateserial \
  -out "$TLS_DIR/server.crt" \
  -days 3650 \
  -extfile "$TMP_DIR/server-ext.cnf"

openssl pkcs12 -export \
  -name "lab3-server" \
  -inkey "$TMP_DIR/server.key" \
  -in "$TLS_DIR/server.crt" \
  -certfile "$TLS_DIR/ca.crt" \
  -out "$TLS_DIR/server-keystore.p12" \
  -passout pass:"$PASSWORD"

openssl req -newkey rsa:2048 -nodes \
  -keyout "$TMP_DIR/client.key" \
  -out "$TMP_DIR/client.csr" \
  -subj "/C=RU/ST=Moscow/L=Moscow/O=Course/OU=SABD/CN=lab3-client"

openssl x509 -req \
  -in "$TMP_DIR/client.csr" \
  -CA "$TLS_DIR/ca.crt" \
  -CAkey "$TMP_DIR/ca.key" \
  -CAcreateserial \
  -out "$TLS_DIR/client.crt" \
  -days 3650 \
  -extfile "$TMP_DIR/client-ext.cnf"

openssl pkcs12 -export \
  -name "lab3-client" \
  -inkey "$TMP_DIR/client.key" \
  -in "$TLS_DIR/client.crt" \
  -certfile "$TLS_DIR/ca.crt" \
  -out "$TLS_DIR/client-keystore.p12" \
  -passout pass:"$PASSWORD"

run_keytool -importcert -noprompt \
  -alias lab3-ca \
  -keystore /tls/server-truststore.p12 \
  -storetype PKCS12 \
  -storepass "$PASSWORD" \
  -file /tls/ca.crt

run_keytool -importcert -noprompt \
  -alias lab3-ca \
  -keystore /tls/client-truststore.p12 \
  -storetype PKCS12 \
  -storepass "$PASSWORD" \
  -file /tls/ca.crt

echo "TLS materials generated in $TLS_DIR"
