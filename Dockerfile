FROM openjdk:21-slim
EXPOSE 8080

ARG JAR_FILE
COPY ${JAR_FILE} app.jar
LABEL authors="jks83"

# ENTRYPOINT ["java", "-Xms512m", "-Xmx2g", "-XX:MaxMetaspaceSize=256m", "-XX:+UseG1GC", "-jar", "/app.jar"]
ENTRYPOINT ["java", "-jar", "/app.jar"]