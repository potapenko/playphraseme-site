FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/uberjar/playphraseme.jar /playphraseme/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/playphraseme/app.jar"]
