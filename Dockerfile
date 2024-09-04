FROM maven:3.5-jdk-8 AS building

COPY . /building

WORKDIR /building

RUN mvn clean package -DSkipTests

FROM openjdk:8-jdk-alpine

WORKDIR /app

COPY --from=building /building/target/91160-cli-jar-with-dependencies.jar /app/91160-cli.jar

ENTRYPOINT ["java", "-jar", "91160-cli.jar"]