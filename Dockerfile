# Start a Maven builder container
FROM maven:3.6-jdk-11-slim as builder

# Allow build args to be set if there is a proxy
ARG proxyHost
ARG proxyPort
ARG SERVER_PORT=8080
ARG DB_ADDRESS=jdbc:postgresql://localhost:5432/Registration
ARG DB_USER=postgres
ARG DB_PASSWORD=postgres
ARG SPRING_PROFILES_ACTIVE=local
ARG AUTH_SECRET

ENV SERVER_PORT=$SERVER_PORT DB_ADDRESS=$DB_ADDRESS DB_USER=$DB_USER DB_PASSWORD=$DB_PASSWORD SPRING_PROFILES_ACTIVE=$SPRING_PROFILES_ACTIVE AUTH_SECRET=$AUTH_SECRET

# Allow additional mvn args to be set if needed
ARG installOpts

RUN mkdir -p /build

WORKDIR /build

COPY . /build

# Run the full compile with maven
RUN mvn install -DproxySet=true -DproxyHost=$proxyHost -DproxyPort=$proxyPort $installOpts
RUN mvn sonar:sonar -Dsonar.host.url=https://sonar.ishtec.cloud \
    -Dsonar.login= \
    -Dsonar.projectKey=Registration_Backend \
    -Dsonar.projectName=Registration_Backend \
    -Dsonar.coverage.jacoco.xmlReportPaths=/build/target/site/jacoco/jacoco.xml \
    -Dsonar.java.coveragePlugin=jacoco \
    -Dsonar.jdbc.dialect=postgresql \
    -Dsonar.language=java \
    -Dsonar.junit.reportPaths=/build/target/surefire-reports/ \
    -Dsonar.java.binaries=/build/target/classes


# App container
FROM adoptopenjdk/openjdk11:x86_64-alpine-jdk-11.0.5_10-slim

RUN apk update && apk upgrade

ARG SERVER_PORT=8080
ARG DB_ADDRESS=jdbc:postgresql://localhost:5432/Registration
ARG DB_USER=postgres
ARG DB_PASSWORD=postgres
ARG SPRING_PROFILES_ACTIVE=local
ARG AUTH_SECRET=

ENV SERVER_PORT=$SERVER_PORT DB_ADDRESS=$DB_ADDRESS DB_USER=$DB_USER DB_PASSWORD=$DB_PASSWORD SPRING_PROFILES_ACTIVE=$SPRING_PROFILES_ACTIVE AUTH_SECRET=$AUTH_SECRET

RUN mkdir /app

WORKDIR /app

EXPOSE $SERVER_PORT $SERVER_PORT

# Add the application's jar to the container
COPY --from=builder /build/* /app/

ENTRYPOINT ["/app/start.sh"]
