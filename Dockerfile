FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/admin-service-0.0.1-SNAPSHOT.jar /app/admin.jar

EXPOSE 8081

CMD ["java", "-jar", "admin.jar"]
