FROM maven:3.5-jdk-8 AS building

COPY . /building

WORKDIR /building

RUN mvn clean package -Dmaven.test.skip=true

FROM openjdk:11-jre-slim-stretch

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && dpkg-reconfigure -f noninteractive tzdata

WORKDIR /app

COPY --from=building /building/target/91160-cli-jar-with-dependencies.jar /app/91160-cli.jar
COPY --from=building /building/entrypoint.sh /app/entrypoint.sh

RUN chmod +x /app/entrypoint.sh

ENV JAVA_OPTS="-Xms512m -Xmx512m -Xmn256m"
ENV APP_CMD=""
ENV APP_CMD_ARGS=""

ENTRYPOINT ["/app/entrypoint.sh"]