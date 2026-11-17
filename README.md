# Loose Cannon Games

Spring Boot 4 / Java 21 site for the fictional Loose Cannon Games studio. Game, news, career, application, account, comment, and support-ticket data is stored with JPA; the production profile uses MySQL.

## Run locally

```sh
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

The local `dev` run uses the bundled H2 database and starter data. Open `http://localhost:8080`. Run the test suite with `./mvnw test`.

## Deploy to EC2

On a fresh Ubuntu 24.04 host, run `sudo bash deploy/setup-ec2.sh`. The script installs MySQL, creates the `lcg` database and a restricted local database account, and stores its generated password in `/etc/lcg/app.env` with mode `600`. Do not copy that file into the repository or expose it in logs. The production profile is selected by that environment file; Hibernate updates the MySQL schema as entities evolve.

Use `deploy/deploy.sh -h <PUBLIC-IP> -i <PATH-TO-PEM>` to test, build, and deploy. The systemd service reads its datasource credentials from `/etc/lcg/app.env`; the app does not fall back to an in-memory database in production.
