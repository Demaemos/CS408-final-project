#!/usr/bin/env bash
# deploy/deploy.sh
# Run FROM YOUR LAPTOP to ship the latest code to the server and restart it:
#   ./deploy/deploy.sh -h <PUBLIC-IP> -i ~/keys/mykey.pem
set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")/.."
 
HOST=""
KEY=""
while getopts "h:i:" opt; do
  case "${opt}" in
    h) HOST="${OPTARG}" ;;
    i) KEY="${OPTARG}" ;;
    *) echo "Usage: $0 -h <PUBLIC-IP> -i <path-to-pem>"; exit 1 ;;
  esac
done
 
if [ -z "${HOST}" ] || [ -z "${KEY}" ]; then
  echo "Usage: $0 -h <PUBLIC-IP> -i <path-to-pem>"
  exit 1
fi
 
REMOTE_USER="ubuntu"
REMOTE_DIR="/opt/lcg"
 
echo "==> Running tests..."
./mvnw -q test
 
echo "==> Building the jar..."
./mvnw -q -DskipTests package
JAR_PATH="$(ls target/*.jar | grep -v 'original' | head -1)"
if [ -z "${JAR_PATH}" ]; then
  echo "ERROR: no jar found in target/ after build."
  exit 1
fi
echo "    Built: ${JAR_PATH}"
 
echo "==> Copying jar to the server..."
scp -i "${KEY}" -o StrictHostKeyChecking=accept-new \
  "${JAR_PATH}" "${REMOTE_USER}@${HOST}:${REMOTE_DIR}/app.jar.new"
 
echo "==> Swapping in the new jar and restarting the service on the server..."
ssh -i "${KEY}" -o StrictHostKeyChecking=accept-new "${REMOTE_USER}@${HOST}" bash -s <<'REMOTE'
set -euo pipefail
sudo mv /opt/lcg/app.jar.new /opt/lcg/app.jar
sudo systemctl restart LCG
REMOTE
 
echo "==> Waiting for the app to come up..."
HEALTH_OK="false"
for i in $(seq 1 15); do
  if curl -fsS "http://${HOST}/api/health" >/dev/null 2>&1; then
    HEALTH_OK="true"
    break
  fi
  sleep 2
done
 
echo "==> Health check..."
if [ "${HEALTH_OK}" = "true" ]; then
  echo "==> Deploy succeeded: http://${HOST}/"
else
  echo "ERROR: health check failed at http://${HOST}/api/health after 30s"
  echo "       Check: ssh -i ${KEY} ${REMOTE_USER}@${HOST} 'journalctl -u LCG -n 50'"
  exit 1
fi
 