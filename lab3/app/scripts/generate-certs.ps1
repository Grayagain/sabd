$ErrorActionPreference = "Stop"

$BaseDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$TlsDir = if ($args.Count -ge 1) { $args[0] } else { Join-Path $BaseDir "..\src\main\resources\tls" }
$TmpDir = Join-Path ([System.IO.Path]::GetTempPath()) ([System.Guid]::NewGuid().ToString())
$Password = "changeit"

function Invoke-Keytool {
  param([string[]]$Arguments)

  if (Get-Command keytool -ErrorAction SilentlyContinue) {
    & keytool @Arguments
    return
  }

  if (Get-Command docker -ErrorAction SilentlyContinue) {
    & docker run --rm -v "${TlsDir}:/tls" eclipse-temurin:21-jdk keytool @Arguments
    return
  }

  throw "keytool or docker is required to create truststores"
}

New-Item -ItemType Directory -Force -Path $TlsDir | Out-Null
New-Item -ItemType Directory -Force -Path $TmpDir | Out-Null
Get-ChildItem -Path $TlsDir -Include *.p12,*.crt -ErrorAction SilentlyContinue | Remove-Item -Force

@"
basicConstraints=CA:FALSE
keyUsage=digitalSignature,keyEncipherment
extendedKeyUsage=serverAuth
subjectAltName=DNS:localhost,IP:127.0.0.1,DNS:lab3-server
"@ | Set-Content -Path (Join-Path $TmpDir "server-ext.cnf")

@"
basicConstraints=CA:FALSE
keyUsage=digitalSignature,keyEncipherment
extendedKeyUsage=clientAuth
"@ | Set-Content -Path (Join-Path $TmpDir "client-ext.cnf")

openssl req -x509 -newkey rsa:2048 -nodes `
  -keyout "$TmpDir/ca.key" `
  -out "$TlsDir/ca.crt" `
  -days 3650 `
  -subj "/C=RU/ST=Moscow/L=Moscow/O=Course/OU=SABD/CN=lab3-ca"

openssl req -newkey rsa:2048 -nodes `
  -keyout "$TmpDir/server.key" `
  -out "$TmpDir/server.csr" `
  -subj "/C=RU/ST=Moscow/L=Moscow/O=Course/OU=SABD/CN=localhost"

openssl x509 -req `
  -in "$TmpDir/server.csr" `
  -CA "$TlsDir/ca.crt" `
  -CAkey "$TmpDir/ca.key" `
  -CAcreateserial `
  -out "$TlsDir/server.crt" `
  -days 3650 `
  -extfile "$TmpDir/server-ext.cnf"

openssl pkcs12 -export `
  -name "lab3-server" `
  -inkey "$TmpDir/server.key" `
  -in "$TlsDir/server.crt" `
  -certfile "$TlsDir/ca.crt" `
  -out "$TlsDir/server-keystore.p12" `
  -passout pass:$Password

openssl req -newkey rsa:2048 -nodes `
  -keyout "$TmpDir/client.key" `
  -out "$TmpDir/client.csr" `
  -subj "/C=RU/ST=Moscow/L=Moscow/O=Course/OU=SABD/CN=lab3-client"

openssl x509 -req `
  -in "$TmpDir/client.csr" `
  -CA "$TlsDir/ca.crt" `
  -CAkey "$TmpDir/ca.key" `
  -CAcreateserial `
  -out "$TlsDir/client.crt" `
  -days 3650 `
  -extfile "$TmpDir/client-ext.cnf"

openssl pkcs12 -export `
  -name "lab3-client" `
  -inkey "$TmpDir/client.key" `
  -in "$TlsDir/client.crt" `
  -certfile "$TlsDir/ca.crt" `
  -out "$TlsDir/client-keystore.p12" `
  -passout pass:$Password

Invoke-Keytool @(
  '-importcert', '-noprompt',
  '-alias', 'lab3-ca',
  '-keystore', '/tls/server-truststore.p12',
  '-storetype', 'PKCS12',
  '-storepass', $Password,
  '-file', '/tls/ca.crt'
)

Invoke-Keytool @(
  '-importcert', '-noprompt',
  '-alias', 'lab3-ca',
  '-keystore', '/tls/client-truststore.p12',
  '-storetype', 'PKCS12',
  '-storepass', $Password,
  '-file', '/tls/ca.crt'
)

Remove-Item -Recurse -Force $TmpDir

Write-Host "TLS materials generated in $TlsDir"
