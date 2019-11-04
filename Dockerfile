FROM openjdk:8-alpine

COPY target/uberjar/playphraseme.jar /playphraseme/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/playphraseme/app.jar"]
