#!/usr/bin/env bash
# deploy/setup-ec2.sh
# One-time setup of a fresh EC2 server. Run ON THE SERVER with sudo:
#   sudo bash deploy/setup-ec2.sh
set -euo pipefail
 
APP_NAME="lcg"
APP_DIR="/opt/${APP_NAME}"
SERVICE_SRC="$(dirname "${BASH_SOURCE[0]}")/LCG.service"
SERVICE_DEST="/etc/systemd/system/LCG.service"
ENV_DIR="/etc/${APP_NAME}"
 
echo "==> Updating packages..."
apt-get update -y
apt-get upgrade -y
 
echo "==> Installing Java 21, nginx, git, and MySQL..."
apt-get install -y openjdk-21-jdk nginx git curl mysql-server openssl
 
echo "==> Creating app directory ${APP_DIR}..."
mkdir -p "${APP_DIR}"
chown ubuntu:ubuntu "${APP_DIR}"
 
echo "==> Creating env directory ${ENV_DIR} (secrets go here later, never in Git)..."
mkdir -p "${ENV_DIR}"
if [ ! -f "${ENV_DIR}/app.env" ]; then
  cat > "${ENV_DIR}/app.env" <<'EOF'
# Placeholder environment file. Fill in real values on the server only;
# never commit an actual app.env to Git.
SPRING_PROFILES_ACTIVE=prod
EOF
fi

DB_PASSWORD="$(sed -n 's/^SPRING_DATASOURCE_PASSWORD=//p' "${ENV_DIR}/app.env" | tail -n 1)"
if [ -z "${DB_PASSWORD}" ]; then
  DB_PASSWORD="$(openssl rand -hex 24)"
  sed -i '/^SPRING_DATASOURCE_PASSWORD=/d' "${ENV_DIR}/app.env"
  printf '\nSPRING_DATASOURCE_PASSWORD=%s\n' "${DB_PASSWORD}" >> "${ENV_DIR}/app.env"
fi
if ! grep -q '^SPRING_DATASOURCE_URL=' "${ENV_DIR}/app.env"; then
  printf 'SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/lcg?useSSL=false&serverTimezone=UTC\n' >> "${ENV_DIR}/app.env"
fi
if ! grep -q '^SPRING_DATASOURCE_USERNAME=' "${ENV_DIR}/app.env"; then
  printf 'SPRING_DATASOURCE_USERNAME=lcg_app\n' >> "${ENV_DIR}/app.env"
fi

echo "==> Creating the application database and restricted local user..."
systemctl enable --now mysql
mysql --protocol=socket -uroot <<SQL
CREATE DATABASE IF NOT EXISTS lcg CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'lcg_app'@'localhost' IDENTIFIED BY '${DB_PASSWORD}';
ALTER USER 'lcg_app'@'localhost' IDENTIFIED BY '${DB_PASSWORD}';
GRANT ALL PRIVILEGES ON lcg.* TO 'lcg_app'@'localhost';
FLUSH PRIVILEGES;
SQL

chown root:root "${ENV_DIR}/app.env"
chmod 600 "${ENV_DIR}/app.env"
 
echo "==> Installing systemd service..."
cp "${SERVICE_SRC}" "${SERVICE_DEST}"
systemctl daemon-reload
systemctl enable LCG
 
echo "==> Configuring nginx to forward port 80 -> 8080..."
cat > /etc/nginx/sites-available/${APP_NAME} <<'EOF'
server {
    listen 80;
    server_name _;
 
    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
EOF
ln -sf /etc/nginx/sites-available/${APP_NAME} /etc/nginx/sites-enabled/${APP_NAME}
rm -f /etc/nginx/sites-enabled/default
nginx -t
systemctl restart nginx
systemctl enable nginx
 
echo "==> Starting the app service..."
systemctl restart LCG || echo "NOTE: LCG service not started yet - deploy your jar first with deploy/deploy.sh"
 
echo "==> Server setup complete."
echo "    App directory: ${APP_DIR}"
echo "    Service:       systemctl status LCG"
echo "    Logs:          journalctl -u LCG -f"
 
