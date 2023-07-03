FROM openjdk:17-jdk-alpine3.14
COPY target/notification.jar /app/
WORKDIR /app
ENTRYPOINT ["sh", "-c", "java -Dspring.profiles.active=${PROFILE} -jar /app/notification.jar"]
EXPOSE 8086