FROM openjdk:21-slim
EXPOSE 8080

ARG JAR_FILE
COPY ${JAR_FILE} app.jar
LABEL authors="jks83"

ENTRYPOINT ["java", "-Xms128m", "-Xmx256m", "-XX:MaxMetaspaceSize=128m", "-XX:+UseG1GC", "-Xlog:gc*:file=/logs/g1-gc.log:tags,uptime,time,level", "-XX:+HeapDumpOnOutOfMemoryError", "-jar", "/app.jar"]

# 성능을 올린다면... 이걸로..
# ENTRYPOINT ["java", "-Xms512m", "-Xmx2g", "-XX:MaxMetaspaceSize=256m", "-XX:+UseZGC", "-XX:SoftMaxHeapSize=2g", "-Xlog:gc*:file=/logs/zgc.log:tags,uptime,time,level", "-XX:+HeapDumpOnOutOfMemoryError", "-jar", "/app.jar"]

# ENTRYPOINT ["java", "-Xms512m", "-Xmx2g", "-XX:MaxMetaspaceSize=256m", "-XX:+UseG1GC","-Xlog:gc*:file=/logs/g1-gc.log:tags,uptime,time,level", "-jar", "/app.jar"]
# ENTRYPOINT ["java", "-Xms512m", "-Xmx2g", "-XX:MaxMetaspaceSize=256m", "-XX:+UseShenandoahGC", "-Xlog:gc*:file=/logs/shenandoah-gc.log:tags,uptime,time,level", "-jar", "/app.jar"]