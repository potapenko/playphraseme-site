FROM java:8-alpine
MAINTAINER Eugene Potapenko <eugene.john.potapenko@gmail.com>

ADD target/uberjar/playphraseme.jar /playphraseme/app.jar

EXPOSE 3033

CMD ["java", "-jar", "/playphraseme/app.jar"]

# FROM clojure
# MAINTAINER Eugene Potapenko <eugene.john.potapenko@gmail.com>

# COPY . /app

# WORKDIR /app

# EXPOSE 3033

# CMD ["lein", "run"]


