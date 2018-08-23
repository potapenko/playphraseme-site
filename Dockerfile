FROM java:8-alpine
MAINTAINER Eugene Potapenko <eugene.john.potapenko@gmail.com>

ADD target/uberjar/playphraseme.jar /playphraseme/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/playphraseme/app.jar"]


