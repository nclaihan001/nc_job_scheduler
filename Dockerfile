FROM maven:3-adoptopenjdk-11 as build
ADD src /code/src
ADD pom.xml /code/pom.xml
WORKDIR /code
RUN mvn clean install

FROM adoptopenjdk:11-jre
ENV LANG en_US.UTF-8
ENV LANGUAGE en_US:en
ENV LC_ALL en_US.UTF-8
COPY --from=build /code/target/*.jar /app.jar