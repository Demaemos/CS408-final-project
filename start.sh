#!/usr/bin/env bash
# start.sh - sets up and runs the app on any machine, from a fresh clone.
# Safe to run more than once.
set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")"
 
echo "==> Checking required tools..."
 
if ! command -v java >/dev/null 2>&1; then
  echo "ERROR: Java is not installed. Install a JDK (version 21 or newer) and try again."
  exit 1
fi
 
JAVA_MAJOR="$(java -version 2>&1 | head -1 | sed -E 's/.*"([0-9]+).*/\1/')"
if [ "${JAVA_MAJOR:-0}" -lt 21 ]; then
  echo "ERROR: Java 21 or newer is required (found Java ${JAVA_MAJOR:-unknown})."
  exit 1
fi
 
if [ ! -x "./mvnw" ]; then
  echo "ERROR: Maven wrapper (mvnw) not found or not executable in the project root."
  echo "       Run: chmod +x mvnw   and commit that change."
  exit 1
fi
 
echo "==> Installing project dependencies and compiling (this also downloads Maven itself, no local install needed)..."
./mvnw -q -DskipTests dependency:go-offline
./mvnw -q -DskipTests compile
 
echo "==> No manual database setup needed: the dev profile uses an in-memory H2 database with seed data."
 
PORT="${PORT:-8080}"
echo "==> Starting the app on port ${PORT} (profile: dev)..."
echo "==> Once it's up, open: http://localhost:${PORT}"
 
exec ./mvnw -q spring-boot:run -Dspring-boot.run.profiles=dev -Dspring-boot.run.arguments="--server.port=${PORT}"
