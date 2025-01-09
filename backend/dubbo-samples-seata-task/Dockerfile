FROM openjdk:21-jdk-slim
WORKDIR /app

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

COPY target/*.jar app.jar
EXPOSE 8083 20893
ENTRYPOINT ["java","-jar","app.jar"] 