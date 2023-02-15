FROM openjdk:17-alpine

EXPOSE 8080

COPY target/*.jar /app-spring/app.jar

VOLUME /app-spring

WORKDIR /app-spring

CMD java -jar app.jar