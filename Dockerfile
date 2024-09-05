FROM maven:3.5.3-jdk-11-slim AS building

COPY . /building

WORKDIR /building

RUN mvn clean package -Dmaven.test.skip=true

FROM openjdk:11-jre-slim-stretch

WORKDIR /app

COPY --from=building /building/target/91160-cli-jar-with-dependencies.jar /app/91160-cli.jar

ENTRYPOINT ["java", "-jar", "91160-cli.jar"]